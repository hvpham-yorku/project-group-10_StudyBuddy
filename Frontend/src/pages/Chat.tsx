/* 
 * Chat.tsx
 * Main chat interface for one-on-one and group conversations.
 */

import { useState, useRef, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
  Search, Send, Paperclip, MoreVertical,
  Users, Circle, X, File, Image as ImageIcon, Link as LinkIcon
} from "lucide-react";
import { chats, currentUser } from "../data/mockData";

function OnlineDot({ status }: { status: "online" | "offline" | "idle" }) {
  const colors = { online: "bg-green-500", offline: "bg-slate-300", idle: "bg-yellow-400" };
  return <div className={`w-2.5 h-2.5 rounded-full border-2 border-white ${colors[status]}`}></div>;
}

function TypingIndicator() {
  return (
    <div className="flex items-center gap-1.5 px-4 py-1.5">
      <div className="flex items-center gap-1 bg-white rounded-2xl rounded-bl-sm px-3 py-2 shadow-sm border border-slate-100">
        <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce" style={{ animationDelay: "0ms" }}></span>
        <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce" style={{ animationDelay: "150ms" }}></span>
        <span className="w-1.5 h-1.5 bg-slate-400 rounded-full animate-bounce" style={{ animationDelay: "300ms" }}></span>
      </div>
      <span className="text-xs text-slate-400">typing...</span>
    </div>
  );
}

export default function Chat() {
  const directChats = chats.filter((chat) => chat.type === "direct");
  const knownMembers = [currentUser, ...chats.flatMap((chat) => chat.members)].reduce((acc: any[], member: any) => {
    if (!acc.find((existing) => existing.id === member.id)) {
      acc.push(member);
    }
    return acc;
  }, []);

  const toMemberProfile = (userId: string) => {
    return knownMembers.find((member: any) => member.id === userId) || {
      id: userId,
      name: userId,
      avatar: null,
      isOnline: false,
    };
  };

  const API_BASE = ((import.meta as any).env?.VITE_API_BASE_URL || "").replace(/\/$/, "");
  const apiUrl = (path: string) => `${API_BASE}${path}`;
  const { id } = useParams<{ id?: string }>();
  const [selectedChatId, setSelectedChatId] = useState<string>(id || directChats[0]?.id || chats[0].id);
  const [messageInput, setMessageInput] = useState("");
  const [search, setSearch] = useState("");
  const [localChats, setLocalChats] = useState(directChats);
  const [chatBackendIds, setChatBackendIds] = useState<Record<string, string>>({});
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [hasMoreMessages, setHasMoreMessages] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [chatError, setChatError] = useState<string | null>(null);
  const [showAttach, setShowAttach] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeout = useRef<ReturnType<typeof setTimeout>>(null);

  const selectedChat = localChats.find((c) => c.id === selectedChatId)!;

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [selectedChat?.messages]);

  const authHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${currentUser.id}`,
  };

  const loadBackendGroupChats = async () => {
    try {
      const response = await fetch(apiUrl("/api/events"));
      if (!response.ok) {
        return;
      }

      const backendEvents = await response.json();
      const availableGroupChats = (backendEvents || [])
        .filter((event: any) => {
          const participants = Array.isArray(event?.participantIds) ? event.participantIds : [];
          const hostId = event?.hostId;
          return participants.includes(currentUser.id) || hostId === currentUser.id;
        })
        .map((event: any) => {
          const participants = Array.isArray(event?.participantIds) ? event.participantIds : [];
          const hostId = event?.hostId;
          const memberIds = [...new Set([...(hostId ? [hostId] : []), ...participants])];
          const members = memberIds.map((memberId: string) => toMemberProfile(memberId));

          return {
            id: `grp_${event.eventId}`,
            name: event.title || event.course || "Event Chat",
            type: "group",
            eventId: event.eventId,
            isLiveEvent: true,
            members,
            lastMessage: {
              sender: "",
              text: "",
              timestamp: new Date().toISOString(),
              read: true,
            },
            unreadCount: 0,
            messages: [],
          } as any;
        });

      setLocalChats([...directChats, ...availableGroupChats]);
    } catch {
      setLocalChats(directChats);
    }
  };

  useEffect(() => {
    void loadBackendGroupChats();
  }, []);

  const mapMessageToUi = (chat: typeof chats[0], apiMessage: any) => {
    const sender = chat.members.find((member) => member.id === apiMessage.senderId) as any;
    return {
      id: apiMessage.messageId,
      senderId: apiMessage.senderId,
      senderName: apiMessage.senderName,
      senderAvatar: sender?.avatar || null,
      text: apiMessage.content,
      timestamp: apiMessage.timestamp,
      type: "text" as const,
    };
  };

  const ensureBackendChat = async (chat: typeof chats[0]): Promise<string> => {
    if (chatBackendIds[chat.id]) {
      return chatBackendIds[chat.id];
    }

    let chatId = "";
    if (chat.type === "direct") {
      const other = chat.members.find((member) => member.id !== currentUser.id);
      if (!other) {
        throw new Error("Could not determine direct chat participant");
      }

      const response = await fetch(apiUrl(`/api/chats/direct`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({ userA: currentUser.id, userB: other.id }),
      });
      if (!response.ok) {
        let details = "Failed to create/fetch direct chat";
        try {
          const errorPayload = await response.json();
          if (errorPayload?.error) {
            details = errorPayload.error;
          }
        } catch {
          // ignore parse errors
        }
        throw new Error(details);
      }

      const data = await response.json();
      chatId = data.chatId;
    } else {
      const backendEventId = chat.eventId;
      if (!backendEventId) {
        throw new Error("Could not resolve backend eventId for this group chat");
      }

      const response = await fetch(apiUrl(`/api/chats/event/${backendEventId}`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({
          chatName: chat.name,
        }),
      });
      if (!response.ok) {
        let details = `Failed to create/fetch event chat for eventId '${backendEventId}'`;
        try {
          const errorPayload = await response.json();
          if (errorPayload?.error) {
            details = errorPayload.error;
          }
        } catch {
          // ignore parse errors
        }
        throw new Error(details);
      }

      const data = await response.json();
      chatId = data.chatId;
    }

    setChatBackendIds((prev) => ({ ...prev, [chat.id]: chatId }));
    return chatId;
  };

  const loadMessages = async (chat: typeof chats[0], before?: string, appendOlder = false) => {
    const backendChatId = await ensureBackendChat(chat);

    setIsLoadingMessages(true);
    setChatError(null);
    try {
      const params = new URLSearchParams({ limit: "20" });
      if (before) {
        params.set("before", before);
      }

      const response = await fetch(apiUrl(`/api/chats/${backendChatId}/messages?${params.toString()}`), {
        headers: { Authorization: `Bearer ${currentUser.id}` },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch chat messages");
      }

      const payload = await response.json();
      const pageMessages = (payload.messages || [])
        .map((message: any) => mapMessageToUi(chat, message))
        .reverse();

      setNextCursor(payload.nextCursor || null);
      setHasMoreMessages(Boolean(payload.hasMore));

      setLocalChats((prev) =>
        prev.map((existing) => {
          if (existing.id !== chat.id) {
            return existing;
          }

          const nextMessages = appendOlder
            ? [...pageMessages, ...existing.messages]
            : pageMessages;

          return {
            ...existing,
            messages: nextMessages,
          };
        })
      );
    } catch (error: any) {
      setChatError(error?.message || "Failed to fetch chat messages");
    } finally {
      setIsLoadingMessages(false);
    }
  };

  useEffect(() => {
    const chat = localChats.find((c) => c.id === selectedChatId);
    if (!chat) {
      if (localChats.length > 0) {
        setSelectedChatId(localChats[0].id);
      }
      return;
    }

    void loadMessages(chat);
  }, [selectedChatId]);

  const handleTyping = (val: string) => {
    setMessageInput(val);
    setIsTyping(true);
    if (typingTimeout.current) clearTimeout(typingTimeout.current);
    typingTimeout.current = setTimeout(() => setIsTyping(false), 1500);
  };

  const sendMessage = async () => {
    if (!messageInput.trim()) return;
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat || isSending) return;

    setIsSending(true);
    setChatError(null);
    try {
      const backendChatId = await ensureBackendChat(chat);
      const response = await fetch(apiUrl(`/api/chats/${backendChatId}/messages`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({ chatId: backendChatId, content: messageInput.trim(), type: "TEXT" }),
      });

      if (!response.ok) {
        let details = "Failed to send message";
        try {
          const errorPayload = await response.json();
          if (errorPayload?.error) {
            details = errorPayload.error;
          }
        } catch {
          // ignore parse errors and keep default message
        }
        throw new Error(details);
      }

      const apiMessage = await response.json();
      const newMsg = mapMessageToUi(chat, apiMessage);

      setLocalChats((prev) =>
        prev.map((existing) =>
          existing.id === selectedChatId
            ? {
                ...existing,
                messages: [...existing.messages, newMsg],
                lastMessage: { sender: "You", text: newMsg.text, timestamp: newMsg.timestamp, read: true },
              }
            : existing
        )
      );
      setMessageInput("");
      setIsTyping(false);
    } catch (error: any) {
      setChatError(error?.message || "Failed to send message");
    } finally {
      setIsSending(false);
    }
  };

  const formatTime = (ts: string) => {
    const d = new Date(ts);
    return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const formatChatTime = (ts: string) => {
    const d = new Date(ts);
    const now = new Date();
    if (d.toDateString() === now.toDateString()) return formatTime(ts);
    return d.toLocaleDateString("en-CA", { month: "short", day: "numeric" });
  };

  const filteredChats = localChats.filter(
    (c) => c.name.toLowerCase().includes(search.toLowerCase())
  );

  const getOnlineStatus = (chat: typeof chats[0]): "online" | "offline" | "idle" => {
    if (chat.type === "direct") {
      const other = chat.members.find((m) => m.id !== currentUser.id) as any;
      if (other?.isOnline) return "online";
      return "offline";
    }
    return "online";
  };

  return (
    <div className="flex-1 flex overflow-hidden">
      {/* Chat List Sidebar */}
      <div className="w-72 bg-white border-r border-slate-200 flex flex-col shrink-0">
        <div className="px-4 py-4 border-b border-slate-100">
          <h2 className="text-slate-800 mb-3" style={{ fontWeight: 700, fontSize: "1rem" }}>Messages</h2>
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search conversations..."
              className="w-full pl-8 pr-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
            />
          </div>
        </div>
        <div className="flex-1 overflow-y-auto">
          {filteredChats.map((chat) => {
            const status = getOnlineStatus(chat);
            const isSelected = chat.id === selectedChatId;
            return (
              <div
                key={chat.id}
                onClick={() => setSelectedChatId(chat.id)}
                className={`px-4 py-3.5 cursor-pointer transition-colors border-b border-slate-50 hover:bg-slate-50 ${isSelected ? "bg-blue-50 border-blue-100" : ""}`}
              >
                <div className="flex items-start gap-3">
                  <div className="relative shrink-0">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${chat.type === "group" ? "bg-blue-100" : "bg-orange-100"} overflow-hidden`}>
                      {chat.type === "group" ? (
                        <Users size={18} className="text-blue-600" />
                      ) : (
                        (() => {
                          const other = chat.members.find((m) => m.id !== currentUser.id) as any;
                          return other?.avatar ? (
                            <img src={other.avatar} alt={other.name} className="w-full h-full object-cover" />
                          ) : (
                            <span className="text-orange-600" style={{ fontWeight: 700 }}>{chat.name.charAt(0)}</span>
                          );
                        })()
                      )}
                    </div>
                    <div className="absolute -bottom-0.5 -right-0.5">
                      <OnlineDot status={status} />
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 min-w-0">
                        <p className="text-sm text-slate-800 truncate" style={{ fontWeight: isSelected ? 600 : 500 }}>{chat.name}</p>
                        {(chat as any).isLiveEvent && (
                          <span className="text-[10px] px-1.5 py-0.5 rounded bg-green-100 text-green-700 shrink-0">Live Event</span>
                        )}
                      </div>
                      <span className="text-xs text-slate-400 shrink-0">{formatChatTime(chat.lastMessage.timestamp)}</span>
                    </div>
                    <div className="flex items-center justify-between mt-0.5">
                      <p className="text-xs text-slate-500 truncate">
                        {chat.lastMessage.sender === "You" ? "" : `${chat.lastMessage.sender}: `}
                        {chat.lastMessage.text}
                      </p>
                      {chat.unreadCount > 0 && (
                        <span className="ml-1 min-w-[18px] h-[18px] bg-orange-500 text-white rounded-full flex items-center justify-center shrink-0" style={{ fontSize: "10px", fontWeight: 700 }}>
                          {chat.unreadCount}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Chat Area */}
      {selectedChat ? (
        <div className="flex-1 flex flex-col overflow-hidden bg-slate-50">
          {/* Chat Header */}
          <div className="bg-white border-b border-slate-200 px-5 py-3 flex items-center gap-3 shrink-0">
            <div className={`w-9 h-9 rounded-full flex items-center justify-center ${selectedChat.type === "group" ? "bg-blue-100" : "bg-orange-100"} overflow-hidden`}>
              {selectedChat.type === "group" ? (
                <Users size={16} className="text-blue-600" />
              ) : (
                (() => {
                  const other = selectedChat.members.find((m) => m.id !== currentUser.id) as any;
                  return other?.avatar ? (
                    <img src={other.avatar} alt={other.name} className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-orange-600" style={{ fontWeight: 700 }}>{selectedChat.name.charAt(0)}</span>
                  );
                })()
              )}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <p className="text-sm text-slate-800" style={{ fontWeight: 600 }}>{selectedChat.name}</p>
                {(selectedChat as any).isLiveEvent && (
                  <span className="text-[10px] px-1.5 py-0.5 rounded bg-green-100 text-green-700">Live Event</span>
                )}
              </div>
              <div className="flex items-center gap-1.5">
                <Circle size={7} className="fill-green-500 text-green-500" />
                <p className="text-xs text-slate-400">
                  {selectedChat.type === "group"
                    ? `${selectedChat.members.length} members`
                    : "Active now"}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-1">
              {selectedChat.type === "group" && (
                <button className="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center transition-colors">
                  <Users size={16} className="text-slate-500" />
                </button>
              )}
              <button className="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center transition-colors">
                <MoreVertical size={16} className="text-slate-500" />
              </button>
            </div>
          </div>

          {/* Online Members (for groups) */}
          {selectedChat.type === "group" && (
            <div className="bg-white border-b border-slate-100 px-5 py-2 flex items-center gap-3">
              <p className="text-xs text-slate-400">Members:</p>
              <div className="flex items-center gap-2">
                {selectedChat.members.map((m) => {
                  const member = m as any;
                  return (
                    <div key={m.id} className="flex items-center gap-1">
                      <div className="relative">
                        <div className="w-6 h-6 rounded-full overflow-hidden bg-blue-100">
                          {member.avatar ? (
                            <img src={member.avatar} alt={member.name} className="w-full h-full object-cover" />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontSize: "9px", fontWeight: 700 }}>
                              {member.name.charAt(0)}
                            </div>
                          )}
                        </div>
                        <div className={`absolute -bottom-0.5 -right-0.5 w-2 h-2 rounded-full border border-white ${member.isOnline !== false ? "bg-green-500" : "bg-slate-300"}`}></div>
                      </div>
                      <span className="text-xs text-slate-500">{member.name.split(" ")[0]}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Messages */}
          <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
            {hasMoreMessages && (
              <div className="flex justify-center">
                <button
                  disabled={isLoadingMessages || !nextCursor}
                  onClick={() => nextCursor && selectedChat && void loadMessages(selectedChat, nextCursor, true)}
                  className="text-xs px-3 py-1.5 rounded-md bg-slate-200 hover:bg-slate-300 disabled:opacity-50"
                >
                  {isLoadingMessages ? "Loading..." : "Load older messages"}
                </button>
              </div>
            )}
            {selectedChat.messages.map((msg, idx) => {
              const isMe = msg.senderId === currentUser.id;
              const prevMsg = selectedChat.messages[idx - 1];
              const showSender = !isMe && (!prevMsg || prevMsg.senderId !== msg.senderId);
              return (
                <div key={msg.id} className={`flex ${isMe ? "justify-end" : "justify-start"} gap-2`}>
                  {!isMe && (
                    <div className={`w-7 h-7 rounded-full bg-blue-100 overflow-hidden shrink-0 self-end ${!showSender ? "opacity-0" : ""}`}>
                      {msg.senderAvatar ? (
                        <img src={msg.senderAvatar} alt={msg.senderName} className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontSize: "10px", fontWeight: 700 }}>
                          {msg.senderName.charAt(0)}
                        </div>
                      )}
                    </div>
                  )}
                  <div className={`max-w-xs lg:max-w-sm ${isMe ? "items-end" : "items-start"} flex flex-col`}>
                    {showSender && <p className="text-xs text-slate-400 mb-1 ml-1">{msg.senderName}</p>}
                    <div
                      className={`px-4 py-2.5 rounded-2xl text-sm leading-relaxed ${
                        isMe
                          ? "bg-blue-700 text-white rounded-br-sm"
                          : "bg-white text-slate-800 shadow-sm border border-slate-100 rounded-bl-sm"
                      } ${msg.type === "file" ? "flex items-center gap-2" : ""}`}
                    >
                      {msg.type === "file" ? (
                        <>
                          <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${isMe ? "bg-blue-600" : "bg-orange-50"}`}>
                            <File size={16} className={isMe ? "text-blue-200" : "text-orange-500"} />
                          </div>
                          <div>
                            <p className="text-xs" style={{ fontWeight: 600 }}>{(msg as any).attachment?.name}</p>
                            <p className={`text-xs ${isMe ? "text-blue-200" : "text-slate-400"}`}>{(msg as any).attachment?.size}</p>
                          </div>
                        </>
                      ) : (
                        msg.text
                      )}
                    </div>
                    <p className="text-xs text-slate-400 mt-1 mx-1">{formatTime(msg.timestamp)}</p>
                  </div>
                </div>
              );
            })}

            {/* Typing Indicator (mock - shows occasionally) */}
            {isTyping && selectedChat.messages[selectedChat.messages.length - 1]?.senderId !== currentUser.id && (
              <TypingIndicator />
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Attachment Options */}
          {showAttach && (
            <div className="bg-white border-t border-slate-200 px-4 py-3 flex gap-3">
              {[
                { icon: File, label: "File", color: "bg-orange-50 text-orange-500" },
                { icon: ImageIcon, label: "Image", color: "bg-green-50 text-green-500" },
                { icon: LinkIcon, label: "Link", color: "bg-blue-50 text-blue-500" },
              ].map(({ icon: Icon, label, color }) => (
                <button
                  key={label}
                  className={`flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl hover:opacity-80 transition-opacity ${color.split(" ")[0]}`}
                  onClick={() => setShowAttach(false)}
                >
                  <Icon size={18} className={color.split(" ")[1]} />
                  <span className="text-xs text-slate-500">{label}</span>
                </button>
              ))}
              <button onClick={() => setShowAttach(false)} className="ml-auto">
                <X size={16} className="text-slate-400" />
              </button>
            </div>
          )}

          {/* Message Input */}
          <div className="bg-white border-t border-slate-200 px-4 py-3 shrink-0">
            {chatError && (
              <div className="mb-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-md px-3 py-2">
                {chatError}
              </div>
            )}
            <div className="flex items-center gap-3">
            <button
              onClick={() => setShowAttach(!showAttach)}
              className={`w-9 h-9 rounded-xl flex items-center justify-center transition-colors ${showAttach ? "bg-blue-100 text-blue-600" : "bg-slate-100 hover:bg-slate-200 text-slate-500"}`}
            >
              <Paperclip size={16} />
            </button>
            <input
              value={messageInput}
              onChange={(e) => handleTyping(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && void sendMessage()}
              placeholder={`Message ${selectedChat.name}...`}
              className="flex-1 px-4 py-2.5 bg-slate-100 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
            />
            <button
              onClick={sendMessage}
              disabled={!messageInput.trim() || isSending}
              className="w-9 h-9 rounded-xl bg-blue-700 hover:bg-blue-800 disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center transition-colors"
            >
              <Send size={15} className="text-white" />
            </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="flex-1 flex items-center justify-center bg-slate-50">
          <div className="text-center">
            <Users size={40} className="text-slate-200 mx-auto mb-3" />
            <p className="text-slate-400 text-sm">Select a conversation</p>
          </div>
        </div>
      )}
    </div>
  );
}