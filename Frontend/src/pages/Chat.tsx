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
  const knownMembers = [currentUser, ...localChats.flatMap((chat) => chat.members)].reduce((acc: any[], member: any) => {
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
    if (typeof window === "undefined") {
      return currentUser.id;
    }
    return window.sessionStorage.getItem(DEV_ACTOR_KEY) || currentUser.id;
  });

  // Load the real user from your token
  const [activeUser, setActiveUser] = useState<any>(currentUser);

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
        console.error(e);
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

  const selectedChat = localChats.find((c) => c.id === selectedChatId)!;

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [selectedChat?.messages]);

  useEffect(() => {
    if (typeof window !== "undefined") {
      window.sessionStorage.setItem(DEV_ACTOR_KEY, activeUser.id);
    }
    setChatBackendIds({});
    setTypingUserIds([]);
    setIsTyping(false);
  }, [activeUser.id]);

  // Pass the real JWT token to the chat endpoints
  const authHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("studyBuddyToken")}`,
  };

  const loadRealChats = async () => {
    if (!activeUser || activeUser.id === currentUser.id) return;
    
    try {
      // 1. Fetch accepted connections (Direct Chats)
      const connRes = await fetch(`/api/connections?userId=${encodeURIComponent(activeUser.id)}`);
      let directChatsList: any[] = [];
      if (connRes.ok) {
        const connectionsData = await connRes.json();
        directChatsList = connectionsData.map((c: any) => ({
          id: `direct_${c.userId}`, 
          name: c.fullName || c.userId,
          type: "direct",
          members: [
            activeUser,
            { id: c.userId, name: c.fullName || c.userId, avatar: c.profilePic, isOnline: c.activityStatus === "online" }
          ],
          lastMessage: { sender: "", text: "Say hello!", timestamp: new Date().toISOString(), read: true },
          unreadCount: 0,
          messages: []
        }));
      }

      // 2. Fetch Events (Group Chats)
      const eventsRes = await fetch(`/api/events`);
      let eventChatsList: any[] = [];
      if (eventsRes.ok) {
        const eventsData = await eventsRes.json();
        // Filter out only the events you are hosting or attending
        const myEvents = eventsData.filter((e: any) => 
          e.host?.id === activeUser.id || (e.participantIds && e.participantIds.includes(activeUser.id))
        );

        eventChatsList = myEvents.map((e: any) => ({
          id: `event_${e.id}`,
          eventId: e.id, // Store the real backend event ID!
          name: e.title || e.course || "Study Session",
          type: "group",
          isLiveEvent: true, // This triggers the cool green badge in the UI
          members: [
            activeUser,
            // Map the other participant IDs so they show up as members
            ...(e.participantIds || []).filter((id: string) => id !== activeUser.id).map((id: string) => ({ id, name: id, isOnline: true }))
          ],
          lastMessage: { sender: "System", text: "Welcome to the event chat!", timestamp: new Date().toISOString(), read: true },
          unreadCount: 0,
          messages: []
        }));
      }

      const combinedChats = [...directChatsList, ...eventChatsList];
      
      // Merge new connections/events with existing messages to prevent flicker
      setLocalChats((prev) => {
        return combinedChats.map((newChat: any) => {
          const existingChat = prev.find((c) => c.id === newChat.id);
          if (existingChat) {
            return {
              ...newChat,
              messages: existingChat.messages,
              lastMessage: existingChat.lastMessage,
            };
          }
          return newChat;
        });
      });
      
      // Select the right chat on initial load using a live functional update
      setSelectedChatId((currentId) => {
        if (!currentId && combinedChats.length > 0) {
          return combinedChats[0].id;
        }
        return currentId;
      });
      
    } catch (e) {
      console.error("Failed to load chats", e);
    }
  };

  useEffect(() => {
    void loadRealChats();
    const interval = setInterval(() => {
      void loadRealChats();
    }, 5000);
    return () => clearInterval(interval);
  }, [activeUser.id]);

  const mapMessageToUi = (chat: any, apiMessage: any) => {
    const sender = chat.members.find((member) => member.id === apiMessage.senderId) as any;
    const rawType = String(apiMessage.type || "TEXT").toUpperCase();
    const uiType: UiMessageType = rawType === "FILE" ? "file" : rawType === "LINK" ? "link" : "text";
    const fileMeta = apiMessage.file
      ? {
        name: apiMessage.file.fileName,
        sizeBytes: Number(apiMessage.file.fileSizeBytes || 0),
        mimeType: apiMessage.file.mimeType,
        storagePath: apiMessage.file.storagePath,
      }
      : undefined;

    const baseMessage = {
      id: apiMessage.messageId,
      senderId: apiMessage.senderId,
      senderName: sender?.name || apiMessage.senderName,
      senderAvatar: sender?.avatar || null,
      text: apiMessage.content,
      timestamp: apiMessage.timestamp,
      type: uiType,
    };

    return fileMeta ? { ...baseMessage, attachment: fileMeta } : baseMessage;
  };

  const isBackendBackedChat = (chat: any) => {
    return true;
  };

  const appendLocalMessage = (
    chatId: string,
    nextMessage: any,
    lastMessageText: string
  ) => {
    setLocalChats((prev) =>
      prev.map((existing) =>
        existing.id === chatId
          ? {
            ...existing,
            messages: [...existing.messages, nextMessage],
            lastMessage: {
              sender: "You",
              text: lastMessageText,
              timestamp: nextMessage.timestamp,
              read: true,
            },
          }
          : existing
      )
    );
  };

  const ensureBackendChat = async (chat: any): Promise<string> => {
    if (chatBackendIds[chat.id]) {
      return chatBackendIds[chat.id];
    }

    let chatId = "";
    if (chat.type === "direct") {
      const other = chat.members.find((member: any) => member.id !== activeUser.id);
      const response = await fetch(apiUrl(`/api/chats/direct`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({ userA: activeUser.id, userB: other.id }),
      });
      if (!response.ok) throw new Error("Failed to create direct chat");
      const data = await response.json();
      chatId = data.chatId;
    } else {
      // It's a Group Chat! Use the real eventId we attached earlier
      const response = await fetch(apiUrl(`/api/chats/event/${chat.eventId}`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({
          chatName: chat.name,
        }),
      });
      if (!response.ok) throw new Error("Failed to create event chat");
      const data = await response.json();
      chatId = data.chatId;
    }

    setChatBackendIds((prev) => ({ ...prev, [chat.id]: chatId }));
    return chatId;
  };

  const loadMessages = async (chat: any, before?: string, appendOlder = false, silent = false) => {
    const backendChatId = await ensureBackendChat(chat);

    if (!silent) setIsLoadingMessages(true);
    setChatError(null);
    try {
      const params = new URLSearchParams({ limit: "20" });
      if (before) {
        params.set("before", before);
      }

      const response = await fetch(apiUrl(`/api/chats/${backendChatId}/messages?${params.toString()}`), {
        headers: { Authorization: `Bearer ${localStorage.getItem("studyBuddyToken")}` },
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
      if (!silent) setIsLoadingMessages(false);
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

    // Set up the polling interval to fetch new messages every 3 seconds
    const interval = setInterval(() => {
    void loadMessages(chat, undefined, false, true);
    }, 3000);

    // Clean up the interval if clicking on different chat
    return () => clearInterval(interval);
  }, [selectedChatId, activeUser.id]);

  const sendTypingStatus = async (chat: any, typing: boolean) => {
    try {
      const backendChatId = await ensureBackendChat(chat);
      await fetch(apiUrl(`/api/chats/${backendChatId}/typing`), {
        method: "PUT",
        headers: authHeaders,
        body: JSON.stringify({ typing }),
      });
    } catch {
      // typing indicator is best-effort; ignore transient errors
    }
  };

  const stopTypingAndNotify = async () => {
    if (!isTyping) return;
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat) return;

    setIsTyping(false);
    await sendTypingStatus(chat, false);
  };

  useEffect(() => {
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat) {
      return;
    }

    if (typingPollInterval.current) {
      clearInterval(typingPollInterval.current);
      typingPollInterval.current = null;
    }

    let cancelled = false;
    const pollTyping = async () => {
      try {
        const backendChatId = await ensureBackendChat(chat);
        const response = await fetch(apiUrl(`/api/chats/${backendChatId}/typing`), {
          headers: { Authorization: `Bearer ${localStorage.getItem("studyBuddyToken")}` },
        });

        if (!response.ok) {
          return;
        }

        const payload = await response.json();
        if (!cancelled) {
          setTypingUserIds(Array.isArray(payload?.typingUserIds) ? payload.typingUserIds : []);
        }
      } catch {
        if (!cancelled) {
          setTypingUserIds([]);
        }
      }
    };

    void pollTyping();
    typingPollInterval.current = setInterval(() => {
      void pollTyping();
    }, 2000);

    return () => {
      cancelled = true;
      if (typingPollInterval.current) {
        clearInterval(typingPollInterval.current);
        typingPollInterval.current = null;
      }
      void stopTypingAndNotify();
    };
  }, [selectedChatId, activeUser.id]);

  const handleTyping = (val: string) => {
    setMessageInput(val);

    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat) {
      return;
    }

    const nextTyping = val.trim().length > 0;
    if (nextTyping && !isTyping) {
      setIsTyping(true);
      void sendTypingStatus(chat, true);
    }

    if (!nextTyping && isTyping) {
      setIsTyping(false);
      void sendTypingStatus(chat, false);
    }

    if (typingTimeout.current) clearTimeout(typingTimeout.current);
    if (nextTyping) {
      typingTimeout.current = setTimeout(() => {
        setIsTyping(false);
        void sendTypingStatus(chat, false);
      }, 1500);
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
      void sendTypingStatus(chat, false);
    } catch (error: any) {
      setChatError(error?.message || "Failed to send message");
    } finally {
      setIsSending(false);
    }
  };

  const isValidHttpUrl = (value: string) => {
    try {
      const parsed = new URL(value);
      return parsed.protocol === "http:" || parsed.protocol === "https:";
    } catch {
      return false;
    }
  };

  const toAttachmentHref = (storagePath?: string, attachmentName?: string) => {
    if (!storagePath) return undefined;
    const hasDownloadName = attachmentName && attachmentName.trim().length > 0;
    const suffix = hasDownloadName
      ? `${storagePath.includes("?") ? "&" : "?"}downloadName=${encodeURIComponent(attachmentName!)}`
      : "";

    if (/^https?:\/\//i.test(storagePath)) {
      return `${storagePath}${suffix}`;
    }
    return apiUrl(`${storagePath}${suffix}`);
  };

  const sendLinkMessage = async () => {
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat || isSending) return;

    const link = messageInput.trim();
    if (!link) {
      setChatError("Enter a URL to send as a link");
      return;
    }
    if (!isValidHttpUrl(link)) {
      setChatError("Please enter a valid http(s) URL");
      return;
    }

    setIsSending(true);
    setChatError(null);
    try {
      const backendChatId = await ensureBackendChat(chat);
      const response = await fetch(apiUrl(`/api/chats/${backendChatId}/messages`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({ chatId: backendChatId, content: link, type: "LINK" }),
      });

      if (!response.ok) {
        let details = "Failed to send link";
        try {
          const errorPayload = await response.json();
          if (errorPayload?.error) details = errorPayload.error;
        } catch {
          // ignore parse errors
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
      setShowAttach(false);
    } catch (error: any) {
      setChatError(error?.message || "Failed to send link");
    } finally {
      setIsSending(false);
    }
  };

  const uploadAndSendFileMessage = async (selectedFile: File) => {
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat || isSending) return;

    setIsSending(true);
    setChatError(null);
    try {
      const backendChatId = await ensureBackendChat(chat);

      const formData = new FormData();
      formData.append("file", selectedFile);
      const uploadResponse = await fetch(apiUrl("/api/uploads"), {
        method: "POST",
        headers: { Authorization: `Bearer ${localStorage.getItem("studyBuddyToken")}` },
        body: formData,
      });

      if (!uploadResponse.ok) {
        if (uploadResponse.status === 413) {
          throw new Error("File is too large (max 20MB)");
        }
        let details = "Failed to upload file";
        try {
          const errorPayload = await uploadResponse.json();
          if (errorPayload?.error) details = errorPayload.error;
        } catch {
          // ignore parse errors
        }
        throw new Error(details);
      }

      const uploadedMeta = await uploadResponse.json();

      const messageResponse = await fetch(apiUrl(`/api/chats/${backendChatId}/messages`), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({
          chatId: backendChatId,
          content: selectedFile.name,
          type: "FILE",
          file: uploadedMeta,
        }),
      });

      if (!messageResponse.ok) {
        let details = "Failed to send file message";
        try {
          const errorPayload = await messageResponse.json();
          if (errorPayload?.error) details = errorPayload.error;
        } catch {
          // ignore parse errors
        }
        throw new Error(details);
      }

      const apiMessage = await messageResponse.json();
      const newMsg = mapMessageToUi(chat, apiMessage);

      setLocalChats((prev) =>
        prev.map((existing) =>
          existing.id === selectedChatId
            ? {
              ...existing,
              messages: [...existing.messages, newMsg],
              lastMessage: { sender: "You", text: `[FILE] ${selectedFile.name}`, timestamp: newMsg.timestamp, read: true },
            }
            : existing
        )
      );
      setShowAttach(false);
    } catch (error: any) {
      setChatError(error?.message || "Failed to upload/send file");
    } finally {
      setIsSending(false);
    }
  };

  const onChooseFile = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    if (!selectedFile) return;
    await uploadAndSendFileMessage(selectedFile);
    event.target.value = "";
  };

  const sendFriendRequest = async () => {
    const chat = localChats.find((existing) => existing.id === selectedChatId);
    if (!chat || chat.type !== "direct" || isSendingFriendRequest) {
      return;
    }

    const target = chat.members.find((member) => member.id !== activeUser.id);
    if (!target) {
      setChatError("Could not determine target user for friend request");
      return;
    }

    setIsSendingFriendRequest(true);
    setChatError(null);
    setFriendRequestNotice(null);
    try {
      const response = await fetch(apiUrl("/api/chats/friend-requests"), {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({ targetUserId: target.id }),
      });

      if (!response.ok) {
        let details = "Failed to send friend request";
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

      setFriendRequestNotice(`Friend request sent to ${target.name}`);
    } catch (error: any) {
      setChatError(error?.message || "Failed to send friend request");
    } finally {
      setIsSendingFriendRequest(false);
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

  const getOnlineStatus = (chat: any): "online" | "offline" | "idle" => {
    if (chat.type === "direct") {
      const other = chat.members.find((m) => m.id !== activeUser.id) as any;
      if (other?.isOnline) return "online";
      return "offline";
    }
    return "online";
  };

  const getChatDisplayName = (chat: any) => {
    if (chat.type !== "direct") {
      return chat.name;
    }
    const other = chat.members.find((member) => member.id !== activeUser.id) as any;
    return other?.name || chat.name;
  };

  const getChatInitial = (chat: any) => {
    const label = getChatDisplayName(chat);
    return label.charAt(0);
  };

  return (
    <div className="flex-1 flex overflow-hidden">
      {/* Chat List Sidebar */}
      <div className="w-72 bg-white border-r border-slate-200 flex flex-col shrink-0">
        <div className="px-4 py-4 border-b border-slate-100">
          <div className="mb-3 flex items-center justify-between gap-2">
            <h2 className="text-slate-800" style={{ fontWeight: 700, fontSize: "1rem" }}>Messages</h2>
            {(import.meta as any).env?.DEV && (
              <select
                value={activeUser.id}
                onChange={(event) => setDevActorId(event.target.value)}
                className="text-xs border border-slate-200 rounded-md px-2 py-1 bg-white text-slate-600"
                title="Dev Actor"
              >
                {devActors.map((actor: any) => (
                  <option key={actor.id} value={actor.id}>
                    {actor.name.split(" ")[0]} ({actor.id})
                  </option>
                ))}
              </select>
            )}
          </div>
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
                          const other = chat.members.find((m) => m.id !== activeUser.id) as any;
                          return other?.avatar ? (
                            <img src={other.avatar} alt={other.name} className="w-full h-full object-cover" />
                          ) : (
                            <span className="text-orange-600" style={{ fontWeight: 700 }}>{getChatInitial(chat)}</span>
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
                        <p className="text-sm text-slate-800 truncate" style={{ fontWeight: isSelected ? 600 : 500 }}>{getChatDisplayName(chat)}</p>
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
                  const other = selectedChat.members.find((m) => m.id !== activeUser.id) as any;
                  return other?.avatar ? (
                    <img src={other.avatar} alt={other.name} className="w-full h-full object-cover" />
                  ) : (
                    <span className="text-orange-600" style={{ fontWeight: 700 }}>{getChatInitial(selectedChat)}</span>
                  );
                })()
              )}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <p className="text-sm text-slate-800" style={{ fontWeight: 600 }}>{getChatDisplayName(selectedChat)}</p>
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
              {selectedChat.type === "direct" && (
                <button
                  onClick={() => void sendFriendRequest()}
                  disabled={isSendingFriendRequest}
                  className="h-8 px-3 rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 disabled:opacity-50 disabled:cursor-not-allowed text-xs"
                >
                  {isSendingFriendRequest ? "Sending..." : "Send Friend Request"}
                </button>
              )}
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
              const isMe = msg.senderId === activeUser.id;
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
                      className={`px-4 py-2.5 rounded-2xl text-sm leading-relaxed ${isMe
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
                            <a
                              href={toAttachmentHref((msg as any).attachment?.storagePath, (msg as any).attachment?.name)}
                              target="_blank"
                              rel="noreferrer"
                              download={(msg as any).attachment?.name || msg.text}
                              className={`text-xs underline ${isMe ? "text-white" : "text-blue-700"}`}
                              style={{ fontWeight: 600 }}
                            >
                              {(msg as any).attachment?.name || msg.text}
                            </a>
                            <p className={`text-xs ${isMe ? "text-blue-200" : "text-slate-400"}`}>
                              {(msg as any).attachment?.sizeBytes ? `${Math.ceil((msg as any).attachment.sizeBytes / 1024)} KB` : "Attachment"}
                            </p>
                          </div>
                        </>
                      ) : msg.type === "link" ? (
                        <a
                          href={msg.text}
                          target="_blank"
                          rel="noreferrer"
                          className={isMe ? "underline text-white" : "underline text-blue-700"}
                        >
                          {msg.text}
                        </a>
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
            {(typingUserIds.length > 0 || isTyping) && (
              <div>
                <TypingIndicator />
                <p className="text-xs text-slate-400 px-4">
                  {typingUserIds.length > 0
                    ? formatTypingLabel(typingUserIds.map((userId) => toMemberProfile(userId).name.split(" ")[0]))
                    : "You are typing..."}
                </p>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Attachment Options */}
          {showAttach && (
            <div className="bg-white border-t border-slate-200 px-4 py-3 flex gap-3">
              <button
                className="flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl hover:opacity-80 transition-opacity bg-orange-50"
                onClick={() => fileInputRef.current?.click()}
              >
                <File size={18} className="text-orange-500" />
                <span className="text-xs text-slate-500">File</span>
              </button>
              <button
                className="flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl hover:opacity-80 transition-opacity bg-green-50"
                onClick={() => fileInputRef.current?.click()}
              >
                <ImageIcon size={18} className="text-green-500" />
                <span className="text-xs text-slate-500">Image</span>
              </button>
              <button
                className="flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl hover:opacity-80 transition-opacity bg-blue-50"
                onClick={() => void sendLinkMessage()}
              >
                <LinkIcon size={18} className="text-blue-500" />
                <span className="text-xs text-slate-500">Send Link</span>
              </button>
              <button onClick={() => setShowAttach(false)} className="ml-auto">
                <X size={16} className="text-slate-400" />
              </button>
            </div>
          )}

          {/* Message Input */}
          <div className="bg-white border-t border-slate-200 px-4 py-3 shrink-0">
            {friendRequestNotice && (
              <div className="mb-2 text-xs text-green-700 bg-green-50 border border-green-200 rounded-md px-3 py-2">
                {friendRequestNotice}
              </div>
            )}
            {chatError && (
              <div className="mb-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-md px-3 py-2">
                {chatError}
              </div>
            )}
            <div className="flex items-center gap-3">
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                onChange={(event) => void onChooseFile(event)}
              />
              <button
                onClick={() => setShowAttach(!showAttach)}
                className={`w-9 h-9 rounded-xl flex items-center justify-center transition-colors ${showAttach ? "bg-blue-100 text-blue-600" : "bg-slate-100 hover:bg-slate-200 text-slate-500"}`}
              >
                <Paperclip size={16} />
              </button>
              <input
                value={messageInput}
                onChange={(e) => handleTyping(e.target.value)}
                onBlur={() => void stopTypingAndNotify()}
                onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && void sendMessage()}
                placeholder={`Message ${getChatDisplayName(selectedChat)}...`}
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