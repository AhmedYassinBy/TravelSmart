import { Component, ElementRef, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { trigger, transition, style, animate, state } from '@angular/animations';
import { ChatbotService, ChatMessage as ApiChatMessage } from '../../../services/chatbot.service';
import { Subscription } from 'rxjs';

interface ChatMessage {
  id: string;
  content: string;
  sender: 'user' | 'bot';
  timestamp: Date;
  status?: 'sending' | 'sent' | 'error';
}

interface QuickReply {
  text: string;
  action: string;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css'],
  animations: [
    trigger('slideInOut', [
      state('void', style({
        transform: 'translateY(20px) scale(0.95)',
        opacity: 0
      })),
      state('*', style({
        transform: 'translateY(0) scale(1)',
        opacity: 1
      })),
      transition('void => *', animate('300ms cubic-bezier(0.4, 0, 0.2, 1)')),
      transition('* => void', animate('200ms cubic-bezier(0.4, 0, 0.2, 1)'))
    ]),
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),
    trigger('bounceIn', [
      transition(':enter', [
        style({ transform: 'scale(0)' }),
        animate('400ms cubic-bezier(0.68, -0.55, 0.265, 1.55)', style({ transform: 'scale(1)' }))
      ])
    ])
  ]
})
export class ChatbotComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  @ViewChild('messageInput') private messageInput!: ElementRef;

  isOpen = false;
  isMinimized = false;
  messages: ChatMessage[] = [];
  newMessage = '';
  isTyping = false;
  unreadCount = 0;
  isOnline = true;

  quickReplies: QuickReply[] = [
    { text: '✈️ Manage Flights', action: 'manage_flights' },
    { text: '🏨 Manage Hotels', action: 'manage_hotels' },
    { text: '📋 View Bookings', action: 'view_bookings' },
    { text: '❓ Help', action: 'help' }
  ];

  private chatSubscription?: Subscription;
  private conversationHistory: ApiChatMessage[] = [];

  constructor(private chatbotService: ChatbotService) {}

  ngOnInit(): void {
    // Check if the chatbot service is online
    this.chatbotService.checkHealth().subscribe(isOnline => {
      this.isOnline = isOnline;
    });
    
    // Add welcome message when component initializes
    this.addBotMessage('Hello! 👋 I\'m your TravelSmart Admin Assistant. I can help you manage flights, hotels, bookings, and more. How can I assist you today?');
  }

  ngOnDestroy(): void {
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    this.isMinimized = false;
    if (this.isOpen) {
      this.unreadCount = 0;
      setTimeout(() => {
        this.focusInput();
        this.scrollToBottom();
      }, 100);
    }
  }

  minimizeChat(): void {
    this.isMinimized = true;
  }

  closeChat(): void {
    this.isOpen = false;
    this.isMinimized = false;
  }

  sendMessage(): void {
    const content = this.newMessage.trim();
    if (!content || this.isTyping) return;

    const userMessage: ChatMessage = {
      id: this.generateId(),
      content,
      sender: 'user',
      timestamp: new Date(),
      status: 'sending'
    };

    this.messages.push(userMessage);
    this.newMessage = '';
    this.scrollToBottom();

    // Add to conversation history for context
    this.conversationHistory.push({
      role: 'user',
      content
    });

    // Call the real API
    this.sendToApi(content, userMessage);
  }

  handleQuickReply(reply: QuickReply): void {
    const userMessage: ChatMessage = {
      id: this.generateId(),
      content: reply.text,
      sender: 'user',
      timestamp: new Date(),
      status: 'sending'
    };

    this.messages.push(userMessage);
    this.scrollToBottom();

    // Add to conversation history
    this.conversationHistory.push({
      role: 'user',
      content: reply.text
    });

    // Send quick reply as a message to get AI response
    this.sendToApi(this.getQuickReplyPrompt(reply.action), userMessage);
  }

  private getQuickReplyPrompt(action: string): string {
    switch (action) {
      case 'manage_flights':
        return 'I want to manage flights. Can you guide me through the flight management features?';
      case 'manage_hotels':
        return 'I need help with hotel management. What can I do in the hotel management section?';
      case 'view_bookings':
        return 'I want to view and manage bookings. How can I access and filter bookings?';
      case 'help':
        return 'I need help. What features are available in the admin dashboard and how can you assist me?';
      default:
        return action;
    }
  }

  private sendToApi(message: string, userMessage: ChatMessage): void {
    this.isTyping = true;
    this.scrollToBottom();

    this.chatSubscription = this.chatbotService.sendMessage(message, this.conversationHistory).subscribe({
      next: (response) => {
        this.isTyping = false;
        userMessage.status = 'sent';

        if (response.success && response.message) {
          // Add bot response to conversation history
          this.conversationHistory.push({
            role: 'assistant',
            content: response.message
          });
          this.addBotMessage(response.message);
        } else {
          // Handle error
          this.addBotMessage(response.error || 'Sorry, I encountered an issue. Please try again.');
        }
      },
      error: (error) => {
        console.error('Chat error:', error);
        this.isTyping = false;
        userMessage.status = 'error';
        this.addBotMessage('Sorry, I\'m having trouble connecting. Please check your connection and try again.');
      }
    });
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  retryMessage(message: ChatMessage): void {
    if (message.status === 'error') {
      message.status = 'sending';
      this.sendToApi(message.content, message);
    }
  }

  clearChat(): void {
    this.messages = [];
    this.conversationHistory = [];
    this.addBotMessage('Chat cleared. How can I help you today?');
  }

  private addBotMessage(content: string): void {
    const botMessage: ChatMessage = {
      id: this.generateId(),
      content,
      sender: 'bot',
      timestamp: new Date()
    };

    this.messages.push(botMessage);
    
    if (!this.isOpen) {
      this.unreadCount++;
    }
    
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        const element = this.messagesContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
    }, 50);
  }

  private focusInput(): void {
    if (this.messageInput) {
      this.messageInput.nativeElement.focus();
    }
  }

  private generateId(): string {
    return Math.random().toString(36).substring(2, 15);
  }
}
