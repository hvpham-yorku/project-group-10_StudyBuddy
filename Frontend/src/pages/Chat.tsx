/* * Chat.tsx
 * Main chat interface for one-on-one and group conversations.
 */

import { useState, useRef, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
  Search, Send, Paperclip, MoreVertical,
  Users, Circle, X, File, Image as ImageIcon, Link as LinkIcon
} from "lucide-react";
import { currentUser } from "../data/mockData";

type UiMessageType = "text" | "link" | "file";

type UiAttachment = {
  name: string;
  sizeBytes: number;
  mimeType?: string;
  storagePath?: string;
};

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

function formatTypingLabel(names: string[]) {
  if (names.length === 0) return "";
  if (names.length === 1) return `${names[0]} is typing...`;
  if (names.length === 2) return `${names[0]} and ${names[1]} are typing...`;
  return `${names[0]} and ${names.length - 1} others are typing...`;
}

const DEV_ACTOR_KEY = "studybuddy.dev.actorId";

export default function Chat() {
  const [localChats, setLocalChats] = useState<any[]>([]);
  const knownMembers = [currentUser, ...localChats.flatMap((chat) => chat.members || [])].reduce((acc: any[], member: any) => {
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
  const [devActorId, setDevActorId] = useState<string>(() => {
    if (typeof window === "undefined") return currentUser.id;
    return window.sessionStorage.getItem(DEV_ACTOR_KEY) || currentUser.id;
  });

  const [activeUser, setActiveUser] = useState<any>(currentUser);

  // Initialize the real user if they are logged in
  useEffect(() => {
    async function initUser() {
      const token = localStorage.getItem("studyBuddyToken");
      if (!token) return;
      try {
        const res = await fetch("/api/studentcontroller/profile", {
          headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setActiveUser({
            id: data.userId,
            name: data.fullName || data.userId,
            avatar: data.avatar || null,
            isOnline: true
          });
        }
      } catch (e) {
        console.error("Failed to fetch user profile", e);
      }
    }
    initUser();
  }, []);

  const devActors = knownMembers.filter((member: any) => member.id === "u1" || member.id === "u2");
  const { id } = useParams<{ id?: string }>();
  const [selectedChatId, setSelectedChatId] = useState<string>(id || "");
  const [messageInput, setMessageInput] = useState("");
  const [search, setSearch] = useState("");
  const [chatBackendIds, setChatBackendIds] = useState<Record<string, string>>({});
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [hasMoreMessages, setHasMoreMessages] = useState(false);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [isSendingFriendRequest, setIsSendingFriendRequest] = useState(false);
  const [friendRequestNotice, setFriendRequestNotice] = useState<string | null>(null);
  const [chatError, setChatError] = useState<string | null>(null);
  const [showAttach, setShowAttach] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const [typingUserIds, setTypingUserIds] = useState<string[]>([]);
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeout = useRef<ReturnType<typeof setTimeout>>(null);
  const typingPollInterval = useRef<ReturnType<typeof setInterval> | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const selectedChat = localChats.find((c) => c.id === selectedChatId);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [selectedChat?.messages]);

  useEffect(() => {
    if (typeof window !== "undefined" && activeUser?.id) {
      window.sessionStorage.setItem(DEV_ACTOR_KEY, activeUser.id);
    }
    setChatBackendIds({});
    setTypingUserIds([]);
    setIsTyping(false);
  }, [activeUser?.id]);

  const authHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("studyBuddyToken") || activeUser?.id}`,
  };

  const loadRealChats = async () => {
    if (!activeUser || !activeUser.id) return; // FIXED: Removed the ID trap!
    
    try {
      // 0. Fetch global students directory to properly map names!
      let allStudents: any[] = [];
      try {
        const stuRes = await fetch("/api/studentcontroller/getstudents");
        if (stuRes.ok) allStudents = await stuRes.json();
      } catch (e) {
        console.warn("Could not load global directory", e);
      }

      // Helper to safely resolve a user's name
      const resolveUser = (userId: string, fallbackName?: string, fallbackAvatar?: string) => {
        if (userId === activeUser.id) return activeUser;
        const student = allStudents.find((s: any) => s.userId === userId);
        return {
          id: userId,
          name: student ? student.fullName : (fallbackName || userId),
          avatar: student ? student.avatar : (fallbackAvatar || null),
          isOnline: student ? student.isOnline : true
        };
      };

      // 1. Fetch accepted connections (Direct Chats)
      let directChatsList: any[] = [];
      let connectionsData: any[] = [];
      try {
        const connRes = await fetch(`/api/connections?userId=${encodeURIComponent(activeUser.id)}`);
        if (connRes.ok) {
          connectionsData = await connRes.json();
          directChatsList = connectionsData.map((c: any) => ({
            id: `direct_${c.userId}`, 
            name: resolveUser(c.userId, c.fullName).name,
            type: "direct",
            members: [ activeUser, resolveUser(c.userId, c.fullName, c.profilePic) ],
            lastMessage: { sender: "", text: "Say hello!", timestamp: new Date().toISOString(), read: true },
            unreadCount: 0,
            messages: []
          }));
        }
      } catch (e) {}

      // 2. Fetch Events (Group Chats)
      let eventChatsList: any[] = [];
      try {
        const eventsRes = await fetch(`/api/events`);
        if (eventsRes.ok) {
          const eventsData = await eventsRes.json();
          const myEvents = eventsData.filter((e: any) => 
            e.host?.id === activeUser.id || (e.attendees && e.attendees.includes(activeUser.id))
          );

          eventChatsList = myEvents.map((e: any) => {
            const membersList = [
              activeUser,
              ...[...(e.attendees || []), e.host?.id]
                .filter((id: string) => id && id !== activeUser.id)
                .map((id: string) => {
                  const friend = connectionsData.find((c: any) => c.userId === id);
                  const isHost = e.host?.id === id;
                  // Use the helper to guarantee a name is found
                  return resolveUser(id, friend ? friend.fullName : (isHost ? e.host?.name : id), friend ? friend.profilePic : (isHost ? e.host?.avatar : null));
                })
            ];

            const uniqueMembers = Array.from(new Map(membersList.map(m => [m.id, m])).values());

            return {
              id: `event_${e.id}`,
              eventId: e.id, 
              name: e.title || e.course || "Study Session",
              type: "group",
              isLiveEvent: true, 
              members: uniqueMembers,
              lastMessage: { sender: "System", text: "Welcome to the event chat!", timestamp: new Date().toISOString(), read: true },
              unreadCount: 0,
              messages: []
            };
          });
        }
      } catch (e) {}

      const combinedChats = [...directChatsList, ...eventChatsList];
      
      setLocalChats((prev) => {
        return combinedChats.map((newChat: any) => {
          const existingChat = prev.find((c) => c.id === newChat.id);
          return existingChat ? { ...newChat, messages: existingChat.messages, lastMessage: existingChat.lastMessage } : newChat;
        });
      });
      
      setSelectedChatId((currentId) => {
        if (!currentId && combinedChats.length > 0) return combinedChats[0].id;
        return currentId;
      });
      
    } catch (e) {
      console.error("Failed to load chats", e);
    }
  };

  useEffect(() => {
    void loadRealChats();
    const interval = setInterval(() => { void loadRealChats(); }, 5000);
    return () => clearInterval(interval);
  }, [activeUser?.id]);

  const mapMessageToUi = (chat: any, apiMessage: any) => {
    const sender = chat.members.find((member: any) => member.id === apiMessage.senderId) as any;
    const rawType = String(apiMessage.type || "TEXT").toUpperCase();
    const uiType: UiMessageType = rawType === "FILE" ? "file" : rawType === "LINK" ? "link" : "text";
    
    return {
      id: apiMessage.messageId,
      senderId: apiMessage.senderId,
      senderName: sender?.name || apiMessage.senderName || apiMessage.senderId,
      senderAvatar: sender?.avatar || null,
      text: apiMessage.content,
      timestamp: apiMessage.timestamp,
      type: uiType,
      ...(apiMessage.file && { attachment: {
        name: apiMessage.file.fileName,
        sizeBytes: Number(apiMessage.file.fileSizeBytes || 0),
        mimeType: apiMessage.file.mimeType,
        storagePath: apiMessage.file.storagePath,
      }})
    };
  };

  const ensureBackendChat = async (chat: any): Promise<string> => {
    if (chatBackendIds[chat.id]) return chatBackendIds[chat.id];

    let chatId = "";
    if (chat.type === "direct") {
      const other = chat.members.find((member: any) => member.id !== activeUser.id);
      const response = await fetch(apiUrl(`/api/chats/direct`), {
        method: "POST", headers: authHeaders,
        body: JSON.stringify({ userA: activeUser.id, userB: other?.id }),
      });
      if (!response.ok) throw new Error("Failed to create direct chat");
      chatId = (await response.json()).chatId;
    } else {
      const response = await fetch(apiUrl(`/api/chats/event/${chat.eventId}`), {
        method: "POST", headers: authHeaders,
        body: JSON.stringify({ chatName: chat.name }),
      });
      if (!response.ok) throw new Error("Failed to create event chat");
      chatId = (await response.json()).chatId;
    }

    setChatBackendIds((prev) => ({ ...prev, [chat.id]: chatId }));
    return chatId;
  };

  const loadMessages = async (chat: any, before?: string, appendOlder = false, silent = false) => {
    if (!chat) return;
    const backendChatId = await ensureBackendChat(chat);
    if (!silent) setIsLoadingMessages(true);
    setChatError(null);
    try {
      const params = new URLSearchParams({ limit: "20" });
      if (before) params.set("before", before);

      const response = await fetch(apiUrl(`/api/chats/${backendChatId}/messages?${params.toString()}`), {
        headers: authHeaders,
      });

      if (!response.ok) throw new Error("Failed to fetch chat messages");

      const payload = await response.json();
      const pageMessages = (payload.messages || []).map((msg: any) => mapMessageToUi(chat, msg)).reverse();

      setNextCursor(payload.nextCursor || null);
      setHasMoreMessages(Boolean(payload.hasMore));

      setLocalChats((prev) =>
        prev.map((existing) => existing.id === chat.id ? {
            ...existing,
            messages: appendOlder ? [...pageMessages, ...existing.messages] : pageMessages,
          } : existing
        )
      );
    } catch (error: any) {
      setChatError(error?.message || "Failed to fetch chat messages");
    } finally {
      if (!silent) setIsLoadingMessages(false);
    }
  };

  useEffect(() => {
    const chat = localChats.find((c) => c.id === selectedChatId);
    if (!chat) return;
    void loadMessages(chat);
    const interval = setInterval(() => { void loadMessages(chat, undefined, false, true); }, 3000);
    return () => clearInterval(interval);
  }, [selectedChatId, activeUser?.id]);

  const sendTypingStatus = async (chat: any, typing: boolean) => {
    try {
      const backendChatId = await ensureBackendChat(chat);
      await fetch(apiUrl(`/api/chats/${backendChatId}/typing`), {
        method: "PUT", headers: authHeaders, body: JSON.stringify({ typing }),
      });
    } catch {}
  };

  const stopTypingAndNotify = async () => {
    if (!isTyping) return;
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat) return;
    setIsTyping(false);
    await sendTypingStatus(chat, false);
  };

  const handleTyping = (val: string) => {
    setMessageInput(val);
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat) return;

    const nextTyping = val.trim().length > 0;
    if (nextTyping && !isTyping) { setIsTyping(true); void sendTypingStatus(chat, true); }
    if (!nextTyping && isTyping) { setIsTyping(false); void sendTypingStatus(chat, false); }

    if (typingTimeout.current) clearTimeout(typingTimeout.current);
    if (nextTyping) {
      typingTimeout.current = setTimeout(() => { setIsTyping(false); void sendTypingStatus(chat, false); }, 1500);
    }
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
        method: "POST", headers: authHeaders,
        body: JSON.stringify({ chatId: backendChatId, content: messageInput.trim(), type: "TEXT" }),
      });

      if (!response.ok) throw new Error("Failed to send message");

      const apiMessage = await response.json();
      const newMsg = mapMessageToUi(chat, apiMessage);

      setLocalChats((prev) => prev.map((existing) => existing.id === selectedChatId ? {
          ...existing,
          messages: [...existing.messages, newMsg],
          lastMessage: { sender: "You", text: newMsg.text, timestamp: newMsg.timestamp, read: true },
        } : existing
      ));
      setMessageInput("");
      setIsTyping(false);
      void sendTypingStatus(chat, false);
    } catch (error: any) {
      setChatError("Failed to send message");
    } finally {
      setIsSending(false);
    }
  };

  const sendFriendRequest = async () => {
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat || chat.type !== "direct" || isSendingFriendRequest) return;
    
    const target = chat.members.find((member: any) => member.id !== activeUser.id);
    if (!target) return;

    setIsSendingFriendRequest(true);
    try {
      const response = await fetch(apiUrl("/api/chats/friend-requests"), {
        method: "POST", headers: authHeaders, body: JSON.stringify({ targetUserId: target.id }),
      });
      if (!response.ok) throw new Error("Failed to send friend request");
      setFriendRequestNotice(`Friend request sent to ${target.name}`);
    } catch (error: any) {
      setChatError("Failed to send friend request");
    } finally {
      setIsSendingFriendRequest(false);
    }
  };

  const filteredChats = localChats.filter((c) => c.name.toLowerCase().includes(search.toLowerCase()));

  const getChatDisplayName = (chat: any) => {
    if (!chat) return "";
    if (chat.type !== "direct") return chat.name;
    const other = chat.members.find((member: any) => member.id !== activeUser?.id);
    return other?.name || chat.name;
  };

  return (
    <div className="flex-1 flex overflow-hidden">
      {/* Sidebar */}
      <div className="w-72 bg-white border-r border-slate-200 flex flex-col shrink-0">
        <div className="px-4 py-4 border-b border-slate-100">
          <h2 className="text-slate-800 mb-3" style={{ fontWeight: 700, fontSize: "1rem" }}>Messages</h2>
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              value={search} onChange={(e) => setSearch(e.target.value)}
              placeholder="Search conversations..."
              className="w-full pl-8 pr-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
            />
          </div>
        </div>
        
        <div className="flex-1 overflow-y-auto">
          {filteredChats.map((chat) => {
            const isSelected = chat.id === selectedChatId;
            return (
              <div key={chat.id} onClick={() => setSelectedChatId(chat.id)} className={`px-4 py-3.5 cursor-pointer transition-colors border-b border-slate-50 hover:bg-slate-50 ${isSelected ? "bg-blue-50 border-blue-100" : ""}`}>
                <div className="flex items-start gap-3">
                  <div className="relative shrink-0">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${chat.type === "group" ? "bg-blue-100" : "bg-orange-100"} overflow-hidden`}>
                      {chat.type === "group" ? <Users size={18} className="text-blue-600" /> : <span className="text-orange-600 font-bold">{getChatDisplayName(chat).charAt(0)}</span>}
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <p className="text-sm text-slate-800 truncate font-medium">{getChatDisplayName(chat)}</p>
                      <span className="text-xs text-slate-400 shrink-0">New</span>
                    </div>
                    <div className="flex items-center justify-between mt-0.5">
                      <p className="text-xs text-slate-500 truncate">{chat.lastMessage?.text || "..."}</p>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Main Chat Area */}
      {selectedChat ? (
        <div className="flex-1 flex flex-col overflow-hidden bg-slate-50">
          <div className="bg-white border-b border-slate-200 px-5 py-3 flex items-center gap-3 shrink-0">
            <div className={`w-9 h-9 rounded-full flex items-center justify-center ${selectedChat.type === "group" ? "bg-blue-100" : "bg-orange-100"}`}>
              {selectedChat.type === "group" ? <Users size={16} className="text-blue-600" /> : <span className="text-orange-600 font-bold">{getChatDisplayName(selectedChat).charAt(0)}</span>}
            </div>
            <div className="flex-1">
              <p className="text-sm text-slate-800 font-semibold">{getChatDisplayName(selectedChat)}</p>
              <div className="flex items-center gap-1.5">
                <Circle size={7} className="fill-green-500 text-green-500" />
                <p className="text-xs text-slate-400">{selectedChat.type === "group" ? `${selectedChat.members?.length || 0} members` : "Active now"}</p>
              </div>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
            {selectedChat.messages.map((msg: any) => {
              const isMe = msg.senderId === activeUser?.id;
              return (
                <div key={msg.id} className={`flex ${isMe ? "justify-end" : "justify-start"} gap-2`}>
                  <div className={`max-w-xs lg:max-w-sm flex flex-col ${isMe ? "items-end" : "items-start"}`}>
                    {!isMe && <p className="text-xs text-slate-400 mb-1 ml-1">{msg.senderName}</p>}
                    <div className={`px-4 py-2.5 rounded-2xl text-sm ${isMe ? "bg-blue-700 text-white rounded-br-sm" : "bg-white text-slate-800 shadow-sm border border-slate-100 rounded-bl-sm"}`}>
                      {msg.text}
                    </div>
                  </div>
                </div>
              );
            })}
            <div ref={messagesEndRef} />
          </div>

          <div className="bg-white border-t border-slate-200 px-4 py-3 shrink-0">
            {friendRequestNotice && <div className="mb-2 text-xs text-green-700 bg-green-50 p-2 rounded">{friendRequestNotice}</div>}
            {chatError && <div className="mb-2 text-xs text-red-600 bg-red-50 p-2 rounded">{chatError}</div>}
            
            <div className="flex items-center gap-3">
              <input value={messageInput} onChange={(e) => handleTyping(e.target.value)} onKeyDown={(e) => e.key === "Enter" && void sendMessage()} placeholder="Message..." className="flex-1 px-4 py-2.5 bg-slate-100 rounded-xl text-sm focus:outline-none" />
              <button onClick={sendMessage} disabled={!messageInput.trim() || isSending} className="w-9 h-9 rounded-xl bg-blue-700 text-white flex justify-center items-center">
                <Send size={15} />
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="flex-1 flex items-center justify-center bg-slate-50 text-slate-400 text-sm">Select a conversation</div>
      )}
    </div>
  );
}