# Personal AI Assistant Flutter Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Flutter Android app with 5-tab navigation (Dashboard, Calendar, Chat, Search, Profile) that communicates with the Spring Boot backend via REST, featuring JWT auth, biography timeline, AI chat, real-time search, and learning sessions.

**Architecture:** API-first (no offline cache for MVP). All state managed via Riverpod providers backed by repository classes that call the backend through a shared Dio ApiClient. JWT stored in flutter_secure_storage; go_router handles navigation with auth guard.

**Tech Stack:** Flutter 3.x / Dart, flutter_riverpod 2.5.x, dio 5.4.x, flutter_secure_storage 9.x, go_router 14.x, intl 0.19.x, mocktail 1.x

---

## File Structure

```
personal-ai-assistant-app/
├── pubspec.yaml
├── lib/
│   ├── main.dart
│   ├── core/
│   │   ├── api_client.dart          # Dio singleton + JWT interceptor
│   │   ├── token_storage.dart       # flutter_secure_storage wrapper
│   │   ├── api_response.dart        # ApiResponse<T> deserializer
│   │   ├── app_exception.dart       # AppException class
│   │   └── providers.dart           # core Riverpod providers
│   ├── models/
│   │   ├── user.dart
│   │   ├── calendar_event.dart
│   │   ├── chat_session.dart
│   │   ├── chat_message.dart
│   │   ├── biography_event.dart
│   │   ├── search_history.dart
│   │   └── learning_session.dart
│   ├── features/
│   │   ├── auth/
│   │   │   ├── auth_repository.dart
│   │   │   ├── auth_provider.dart
│   │   │   ├── login_screen.dart
│   │   │   └── register_screen.dart
│   │   ├── home/
│   │   │   ├── home_repository.dart
│   │   │   ├── home_provider.dart
│   │   │   └── home_screen.dart
│   │   ├── calendar/
│   │   │   ├── calendar_repository.dart
│   │   │   ├── calendar_provider.dart
│   │   │   ├── calendar_screen.dart
│   │   │   └── create_event_screen.dart
│   │   ├── chat/
│   │   │   ├── chat_repository.dart
│   │   │   ├── chat_provider.dart
│   │   │   ├── session_list_screen.dart
│   │   │   └── chat_screen.dart
│   │   ├── biography/
│   │   │   ├── biography_repository.dart
│   │   │   ├── biography_provider.dart
│   │   │   ├── biography_chat_screen.dart
│   │   │   └── biography_timeline_screen.dart
│   │   ├── search/
│   │   │   ├── search_repository.dart
│   │   │   ├── search_provider.dart
│   │   │   └── search_screen.dart
│   │   ├── learning/
│   │   │   ├── learning_repository.dart
│   │   │   ├── learning_provider.dart
│   │   │   └── learning_screen.dart
│   │   └── profile/
│   │       └── profile_screen.dart
│   └── router/
│       └── app_router.dart
└── test/
    ├── core/
    │   └── api_client_test.dart
    ├── features/
    │   ├── auth/
    │   │   └── auth_repository_test.dart
    │   ├── calendar/
    │   │   └── calendar_repository_test.dart
    │   ├── chat/
    │   │   └── chat_repository_test.dart
    │   ├── biography/
    │   │   └── biography_repository_test.dart
    │   └── search/
    │       └── search_repository_test.dart
    └── helpers/
        └── mock_api_client.dart
```

---

### Task 1: Project Scaffolding

**Files:**
- Create: `personal-ai-assistant-app/pubspec.yaml`
- Create: `personal-ai-assistant-app/lib/main.dart`

- [ ] **Step 1: Verify Flutter is installed**

```bash
flutter --version
```

If Flutter is not installed:
```bash
# macOS
brew install --cask flutter
# OR download from https://docs.flutter.dev/get-started/install/macos
flutter doctor
```

Expected: Flutter 3.x, Dart 3.x

- [ ] **Step 2: Create Flutter project**

```bash
cd /Users/liqiuhua8/claudework
flutter create --org com.personalai --platforms android personal-ai-assistant-app
cd personal-ai-assistant-app
```

Expected: Project created with standard structure.

- [ ] **Step 3: Replace pubspec.yaml**

Replace the entire content of `pubspec.yaml` with:

```yaml
name: personal_ai_assistant
description: Personal AI Assistant Android App
publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: '>=3.3.0 <4.0.0'

dependencies:
  flutter:
    sdk: flutter
  flutter_riverpod: ^2.5.1
  dio: ^5.4.3+1
  flutter_secure_storage: ^9.2.2
  go_router: ^14.2.7
  intl: ^0.19.0

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^4.0.0
  mocktail: ^1.0.3

flutter:
  uses-material-design: true
```

- [ ] **Step 4: Install dependencies**

```bash
flutter pub get
```

Expected: All packages resolved, no errors.

- [ ] **Step 5: Write lib/main.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'router/app_router.dart';

void main() {
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends ConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);
    return MaterialApp.router(
      title: 'Personal AI Assistant',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
        useMaterial3: true,
      ),
      routerConfig: router,
    );
  }
}
```

- [ ] **Step 6: Verify project runs**

```bash
flutter analyze
```

Expected: No errors (warnings about missing router/appRouterProvider are acceptable at this stage if you create a stub).

- [ ] **Step 7: Commit**

```bash
git init
git add pubspec.yaml pubspec.lock lib/main.dart
git commit -m "feat: scaffold Flutter project with dependencies"
```

---

### Task 2: Core Models

**Files:**
- Create: `lib/models/user.dart`
- Create: `lib/models/calendar_event.dart`
- Create: `lib/models/chat_session.dart`
- Create: `lib/models/chat_message.dart`
- Create: `lib/models/biography_event.dart`
- Create: `lib/models/search_history.dart`
- Create: `lib/models/learning_session.dart`

- [ ] **Step 1: Create lib/models/user.dart**

```dart
class User {
  final int id;
  final String username;
  final String? avatarUrl;

  const User({required this.id, required this.username, this.avatarUrl});

  factory User.fromJson(Map<String, dynamic> json) => User(
        id: json['id'] as int,
        username: json['username'] as String,
        avatarUrl: json['avatarUrl'] as String?,
      );
}
```

- [ ] **Step 2: Create lib/models/calendar_event.dart**

```dart
class CalendarEvent {
  final int id;
  final String title;
  final DateTime startTime;
  final DateTime? endTime;
  final DateTime? remindAt;
  final String source; // 'manual' | 'ai'

  const CalendarEvent({
    required this.id,
    required this.title,
    required this.startTime,
    this.endTime,
    this.remindAt,
    required this.source,
  });

  factory CalendarEvent.fromJson(Map<String, dynamic> json) => CalendarEvent(
        id: json['id'] as int,
        title: json['title'] as String,
        startTime: DateTime.parse(json['startTime'] as String),
        endTime: json['endTime'] != null
            ? DateTime.parse(json['endTime'] as String)
            : null,
        remindAt: json['remindAt'] != null
            ? DateTime.parse(json['remindAt'] as String)
            : null,
        source: json['source'] as String? ?? 'manual',
      );
}
```

- [ ] **Step 3: Create lib/models/chat_session.dart**

```dart
class ChatSession {
  final int id;
  final String mode; // 'chat' | 'biography' | 'learning'
  final String title;
  final DateTime updatedAt;

  const ChatSession({
    required this.id,
    required this.mode,
    required this.title,
    required this.updatedAt,
  });

  factory ChatSession.fromJson(Map<String, dynamic> json) => ChatSession(
        id: json['id'] as int,
        mode: json['mode'] as String,
        title: json['title'] as String,
        updatedAt: DateTime.parse(json['updatedAt'] as String),
      );
}
```

- [ ] **Step 4: Create lib/models/chat_message.dart**

```dart
class ChatMessage {
  final int id;
  final String role; // 'user' | 'assistant'
  final String content;
  final DateTime createdAt;

  const ChatMessage({
    required this.id,
    required this.role,
    required this.content,
    required this.createdAt,
  });

  factory ChatMessage.fromJson(Map<String, dynamic> json) => ChatMessage(
        id: json['id'] as int,
        role: json['role'] as String,
        content: json['content'] as String,
        createdAt: DateTime.parse(json['createdAt'] as String),
      );
}
```

- [ ] **Step 5: Create lib/models/biography_event.dart**

```dart
class BiographyEvent {
  final int id;
  final String eventDate; // 'YYYY' | 'YYYY-MM' | 'YYYY-MM-DD'
  final String title;
  final String content;
  final String category;

  const BiographyEvent({
    required this.id,
    required this.eventDate,
    required this.title,
    required this.content,
    required this.category,
  });

  factory BiographyEvent.fromJson(Map<String, dynamic> json) => BiographyEvent(
        id: json['id'] as int,
        eventDate: json['eventDate'] as String,
        title: json['title'] as String,
        content: json['content'] as String,
        category: json['category'] as String? ?? '',
      );
}
```

- [ ] **Step 6: Create lib/models/search_history.dart**

```dart
class SearchHistory {
  final int id;
  final String query;
  final String summary;
  final List<Map<String, dynamic>> sources;
  final DateTime createdAt;
  final bool starred;

  const SearchHistory({
    required this.id,
    required this.query,
    required this.summary,
    required this.sources,
    required this.createdAt,
    required this.starred,
  });

  factory SearchHistory.fromJson(Map<String, dynamic> json) {
    final rawSources = json['sources'];
    List<Map<String, dynamic>> sources = [];
    if (rawSources is List) {
      sources = rawSources.cast<Map<String, dynamic>>();
    }
    return SearchHistory(
      id: json['id'] as int,
      query: json['query'] as String,
      summary: json['summary'] as String? ?? '',
      sources: sources,
      createdAt: DateTime.parse(json['createdAt'] as String),
      starred: json['starred'] as bool? ?? false,
    );
  }
}
```

- [ ] **Step 7: Create lib/models/learning_session.dart**

```dart
class LearningSession {
  final int id;
  final String subject;
  final String topic;
  final int? score;
  final int? durationMin;
  final DateTime createdAt;

  const LearningSession({
    required this.id,
    required this.subject,
    required this.topic,
    this.score,
    this.durationMin,
    required this.createdAt,
  });

  factory LearningSession.fromJson(Map<String, dynamic> json) => LearningSession(
        id: json['id'] as int,
        subject: json['subject'] as String,
        topic: json['topic'] as String,
        score: json['score'] as int?,
        durationMin: json['durationMin'] as int?,
        createdAt: DateTime.parse(json['createdAt'] as String),
      );
}
```

- [ ] **Step 8: Run analysis**

```bash
flutter analyze lib/models/
```

Expected: No errors.

- [ ] **Step 9: Commit**

```bash
git add lib/models/
git commit -m "feat: add data models for all backend entities"
```

---

### Task 3: Core Layer (ApiClient + TokenStorage + Providers)

**Files:**
- Create: `lib/core/api_response.dart`
- Create: `lib/core/app_exception.dart`
- Create: `lib/core/token_storage.dart`
- Create: `lib/core/api_client.dart`
- Create: `lib/core/providers.dart`
- Create: `test/helpers/mock_api_client.dart`
- Create: `test/core/api_client_test.dart`

- [ ] **Step 1: Write failing test**

Create `test/core/api_client_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:dio/dio.dart';
import '../helpers/mock_api_client.dart';
import 'package:personal_ai_assistant/core/api_response.dart';
import 'package:personal_ai_assistant/core/app_exception.dart';

void main() {
  group('ApiResponse.fromJson', () {
    test('parses success response', () {
      final json = {'success': true, 'message': 'OK', 'data': {'id': 1}};
      final response = ApiResponse<Map<String, dynamic>>.fromJson(
        json,
        (d) => d as Map<String, dynamic>,
      );
      expect(response.success, isTrue);
      expect(response.data!['id'], equals(1));
    });

    test('parses failure response', () {
      final json = {'success': false, 'message': 'Not found', 'data': null};
      final response = ApiResponse<Map<String, dynamic>>.fromJson(
        json,
        (d) => d as Map<String, dynamic>,
      );
      expect(response.success, isFalse);
      expect(response.data, isNull);
    });
  });

  group('AppException', () {
    test('carries message', () {
      const e = AppException('test error');
      expect(e.message, equals('test error'));
      expect(e.toString(), contains('test error'));
    });
  });
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
flutter test test/core/api_client_test.dart
```

Expected: FAIL — classes not found.

- [ ] **Step 3: Create lib/core/app_exception.dart**

```dart
class AppException implements Exception {
  final String message;
  const AppException(this.message);

  @override
  String toString() => 'AppException: $message';
}
```

- [ ] **Step 4: Create lib/core/api_response.dart**

```dart
class ApiResponse<T> {
  final bool success;
  final String message;
  final T? data;

  const ApiResponse({
    required this.success,
    required this.message,
    this.data,
  });

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(dynamic) fromData,
  ) {
    final raw = json['data'];
    return ApiResponse<T>(
      success: json['success'] as bool,
      message: json['message'] as String? ?? '',
      data: raw != null ? fromData(raw) : null,
    );
  }
}
```

- [ ] **Step 5: Create test/helpers/mock_api_client.dart**

```dart
import 'package:mocktail/mocktail.dart';
import 'package:dio/dio.dart';

class MockDio extends Mock implements Dio {}
```

- [ ] **Step 6: Run test to verify it passes**

```bash
flutter test test/core/api_client_test.dart
```

Expected: PASS — 3 tests pass.

- [ ] **Step 7: Create lib/core/token_storage.dart**

```dart
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStorage {
  static const _key = 'jwt_token';
  final FlutterSecureStorage _storage;

  TokenStorage({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  Future<void> save(String token) => _storage.write(key: _key, value: token);
  Future<String?> read() => _storage.read(key: _key);
  Future<void> delete() => _storage.delete(key: _key);
}
```

- [ ] **Step 8: Create lib/core/api_client.dart**

```dart
import 'package:dio/dio.dart';
import 'app_exception.dart';
import 'api_response.dart';
import 'token_storage.dart';

const _baseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'http://10.0.2.2:8080',
);

class ApiClient {
  late final Dio _dio;
  final TokenStorage _tokenStorage;

  ApiClient({TokenStorage? tokenStorage, Dio? dio})
      : _tokenStorage = tokenStorage ?? TokenStorage() {
    _dio = dio ??
        Dio(BaseOptions(
          baseUrl: _baseUrl,
          connectTimeout: const Duration(seconds: 10),
          receiveTimeout: const Duration(seconds: 30),
          headers: {'Content-Type': 'application/json'},
        ));

    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _tokenStorage.read();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) {
        handler.next(error);
      },
    ));
  }

  Future<ApiResponse<T>> get<T>(
    String path,
    T Function(dynamic) fromData,
  ) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>(path);
      return ApiResponse.fromJson(response.data!, fromData);
    } on DioException catch (e) {
      throw _mapError(e);
    }
  }

  Future<ApiResponse<T>> post<T>(
    String path,
    dynamic body,
    T Function(dynamic) fromData,
  ) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(path, data: body);
      return ApiResponse.fromJson(response.data!, fromData);
    } on DioException catch (e) {
      throw _mapError(e);
    }
  }

  Future<ApiResponse<T>> put<T>(
    String path,
    dynamic body,
    T Function(dynamic) fromData,
  ) async {
    try {
      final response = await _dio.put<Map<String, dynamic>>(path, data: body);
      return ApiResponse.fromJson(response.data!, fromData);
    } on DioException catch (e) {
      throw _mapError(e);
    }
  }

  AppException _mapError(DioException e) {
    if (e.response != null) {
      final data = e.response!.data;
      if (data is Map<String, dynamic> && data['message'] != null) {
        return AppException(data['message'] as String);
      }
      return AppException('Server error: ${e.response!.statusCode}');
    }
    return AppException('Network error: ${e.message}');
  }
}
```

- [ ] **Step 9: Create lib/core/providers.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'api_client.dart';
import 'token_storage.dart';

final tokenStorageProvider = Provider<TokenStorage>((ref) => TokenStorage());

final apiClientProvider = Provider<ApiClient>((ref) {
  final tokenStorage = ref.watch(tokenStorageProvider);
  return ApiClient(tokenStorage: tokenStorage);
});
```

- [ ] **Step 10: Run analysis**

```bash
flutter analyze lib/core/
```

Expected: No errors.

- [ ] **Step 11: Commit**

```bash
git add lib/core/ test/core/ test/helpers/
git commit -m "feat: add core API client, token storage, and Riverpod providers"
```

---

### Task 4: Auth Feature

**Files:**
- Create: `lib/features/auth/auth_repository.dart`
- Create: `lib/features/auth/auth_provider.dart`
- Create: `lib/features/auth/login_screen.dart`
- Create: `lib/features/auth/register_screen.dart`
- Create: `test/features/auth/auth_repository_test.dart`

- [ ] **Step 1: Write failing test**

Create `test/features/auth/auth_repository_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:personal_ai_assistant/core/api_client.dart';
import 'package:personal_ai_assistant/core/api_response.dart';
import 'package:personal_ai_assistant/core/token_storage.dart';
import 'package:personal_ai_assistant/features/auth/auth_repository.dart';

class MockApiClient extends Mock implements ApiClient {}
class MockTokenStorage extends Mock implements TokenStorage {}

void main() {
  late MockApiClient mockClient;
  late MockTokenStorage mockStorage;
  late AuthRepository repo;

  setUp(() {
    mockClient = MockApiClient();
    mockStorage = MockTokenStorage();
    repo = AuthRepository(apiClient: mockClient, tokenStorage: mockStorage);
  });

  group('AuthRepository.login', () {
    test('saves token on success', () async {
      when(() => mockClient.post<String>(
            '/api/auth/login',
            any(),
            any(),
          )).thenAnswer((_) async => const ApiResponse(
            success: true,
            message: 'OK',
            data: 'jwt-token-abc',
          ));
      when(() => mockStorage.save(any())).thenAnswer((_) async {});

      await repo.login('user', 'pass');

      verify(() => mockStorage.save('jwt-token-abc')).called(1);
    });

    test('throws AppException on failure', () async {
      when(() => mockClient.post<String>(
            '/api/auth/login',
            any(),
            any(),
          )).thenAnswer((_) async => const ApiResponse(
            success: false,
            message: 'Invalid credentials',
          ));

      expect(() => repo.login('user', 'wrong'), throwsA(isA<Exception>()));
    });
  });
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
flutter test test/features/auth/auth_repository_test.dart
```

Expected: FAIL — AuthRepository not found.

- [ ] **Step 3: Create lib/features/auth/auth_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../core/app_exception.dart';
import '../../core/token_storage.dart';

class AuthRepository {
  final ApiClient _apiClient;
  final TokenStorage _tokenStorage;

  AuthRepository({required ApiClient apiClient, required TokenStorage tokenStorage})
      : _apiClient = apiClient,
        _tokenStorage = tokenStorage;

  Future<void> login(String username, String password) async {
    final response = await _apiClient.post<String>(
      '/api/auth/login',
      {'username': username, 'password': password},
      (d) => d as String,
    );
    if (!response.success || response.data == null) {
      throw AppException(response.message);
    }
    await _tokenStorage.save(response.data!);
  }

  Future<void> register(String username, String password) async {
    final response = await _apiClient.post<String>(
      '/api/auth/register',
      {'username': username, 'password': password},
      (d) => d as String,
    );
    if (!response.success || response.data == null) {
      throw AppException(response.message);
    }
    await _tokenStorage.save(response.data!);
  }

  Future<void> logout() async {
    await _tokenStorage.delete();
  }

  Future<bool> isLoggedIn() async {
    final token = await _tokenStorage.read();
    return token != null;
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
flutter test test/features/auth/auth_repository_test.dart
```

Expected: PASS — 2 tests pass.

- [ ] **Step 5: Create lib/features/auth/auth_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import 'auth_repository.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) => AuthRepository(
      apiClient: ref.watch(apiClientProvider),
      tokenStorage: ref.watch(tokenStorageProvider),
    ));

// Tracks whether user is authenticated
final isAuthenticatedProvider = FutureProvider<bool>((ref) async {
  final repo = ref.watch(authRepositoryProvider);
  return repo.isLoggedIn();
});

// Notifier for login/register/logout actions
class AuthNotifier extends AsyncNotifier<void> {
  @override
  Future<void> build() async {}

  Future<void> login(String username, String password) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() =>
        ref.read(authRepositoryProvider).login(username, password));
  }

  Future<void> register(String username, String password) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() =>
        ref.read(authRepositoryProvider).register(username, password));
  }

  Future<void> logout() async {
    await ref.read(authRepositoryProvider).logout();
  }
}

final authNotifierProvider =
    AsyncNotifierProvider<AuthNotifier, void>(AuthNotifier.new);
```

- [ ] **Step 6: Create lib/features/auth/login_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'auth_provider.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _usernameCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _usernameCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    setState(() { _loading = true; _error = null; });
    try {
      await ref.read(authNotifierProvider.notifier)
          .login(_usernameCtrl.text.trim(), _passwordCtrl.text);
      if (mounted) context.go('/');
    } catch (e) {
      setState(() => _error = e.toString().replaceFirst('AppException: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('登录')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (_error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 16),
                child: Text(_error!, style: const TextStyle(color: Colors.red)),
              ),
            TextField(
              controller: _usernameCtrl,
              decoration: const InputDecoration(labelText: '用户名', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _passwordCtrl,
              obscureText: true,
              decoration: const InputDecoration(labelText: '密码', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: _loading ? null : _login,
                child: _loading
                    ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                    : const Text('登录'),
              ),
            ),
            TextButton(
              onPressed: () => context.push('/register'),
              child: const Text('没有账号？注册'),
            ),
          ],
        ),
      ),
    );
  }
}
```

- [ ] **Step 7: Create lib/features/auth/register_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'auth_provider.dart';

class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});

  @override
  ConsumerState<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  final _usernameCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _loading = false;
  String? _error;

  @override
  void dispose() {
    _usernameCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _register() async {
    setState(() { _loading = true; _error = null; });
    try {
      await ref.read(authNotifierProvider.notifier)
          .register(_usernameCtrl.text.trim(), _passwordCtrl.text);
      if (mounted) context.go('/');
    } catch (e) {
      setState(() => _error = e.toString().replaceFirst('AppException: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('注册')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (_error != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 16),
                child: Text(_error!, style: const TextStyle(color: Colors.red)),
              ),
            TextField(
              controller: _usernameCtrl,
              decoration: const InputDecoration(labelText: '用户名', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _passwordCtrl,
              obscureText: true,
              decoration: const InputDecoration(labelText: '密码', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: _loading ? null : _register,
                child: _loading
                    ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                    : const Text('注册'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
```

- [ ] **Step 8: Run tests**

```bash
flutter test test/features/auth/
```

Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add lib/features/auth/ test/features/auth/
git commit -m "feat: add auth repository, provider, login/register screens"
```

---

### Task 5: App Shell (Router + Bottom Navigation)

**Files:**
- Create: `lib/router/app_router.dart`
- Modify: `lib/main.dart` (already imports appRouterProvider)

- [ ] **Step 1: Create lib/router/app_router.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../features/auth/auth_provider.dart';
import '../features/auth/login_screen.dart';
import '../features/auth/register_screen.dart';
import '../features/home/home_screen.dart';
import '../features/calendar/calendar_screen.dart';
import '../features/calendar/create_event_screen.dart';
import '../features/chat/session_list_screen.dart';
import '../features/chat/chat_screen.dart';
import '../features/biography/biography_chat_screen.dart';
import '../features/biography/biography_timeline_screen.dart';
import '../features/search/search_screen.dart';
import '../features/profile/profile_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/',
    redirect: (context, state) async {
      final isAuth = await ref.read(authRepositoryProvider).isLoggedIn();
      final onAuth = state.matchedLocation == '/login' ||
          state.matchedLocation == '/register';
      if (!isAuth && !onAuth) return '/login';
      if (isAuth && onAuth) return '/';
      return null;
    },
    routes: [
      GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
      GoRoute(path: '/register', builder: (_, __) => const RegisterScreen()),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(path: '/', builder: (_, __) => const HomeScreen()),
          GoRoute(
            path: '/calendar',
            builder: (_, __) => const CalendarScreen(),
            routes: [
              GoRoute(
                path: 'create',
                builder: (_, __) => const CreateEventScreen(),
              ),
            ],
          ),
          GoRoute(
            path: '/chat',
            builder: (_, __) => const SessionListScreen(),
            routes: [
              GoRoute(
                path: ':sessionId',
                builder: (_, state) => ChatScreen(
                  sessionId: int.parse(state.pathParameters['sessionId']!),
                ),
              ),
            ],
          ),
          GoRoute(
            path: '/biography',
            builder: (_, __) => const BiographyChatScreen(),
            routes: [
              GoRoute(
                path: 'timeline',
                builder: (_, __) => const BiographyTimelineScreen(),
              ),
            ],
          ),
          GoRoute(path: '/search', builder: (_, __) => const SearchScreen()),
          GoRoute(path: '/profile', builder: (_, __) => const ProfileScreen()),
        ],
      ),
    ],
  );
});

class AppShell extends StatelessWidget {
  final Widget child;
  const AppShell({super.key, required this.child});

  int _locationToIndex(BuildContext context) {
    final location = GoRouterState.of(context).matchedLocation;
    if (location.startsWith('/calendar')) return 1;
    if (location.startsWith('/chat') || location.startsWith('/biography')) return 2;
    if (location.startsWith('/search')) return 3;
    if (location.startsWith('/profile')) return 4;
    return 0;
  }

  @override
  Widget build(BuildContext context) {
    final currentIndex = _locationToIndex(context);
    return Scaffold(
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: currentIndex,
        onDestinationSelected: (index) {
          switch (index) {
            case 0: context.go('/');
            case 1: context.go('/calendar');
            case 2: context.go('/chat');
            case 3: context.go('/search');
            case 4: context.go('/profile');
          }
        },
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: '首页'),
          NavigationDestination(icon: Icon(Icons.calendar_month_outlined), selectedIcon: Icon(Icons.calendar_month), label: '日历'),
          NavigationDestination(icon: Icon(Icons.chat_outlined), selectedIcon: Icon(Icons.chat), label: '聊天'),
          NavigationDestination(icon: Icon(Icons.search), label: '搜索'),
          NavigationDestination(icon: Icon(Icons.person_outlined), selectedIcon: Icon(Icons.person), label: '我的'),
        ],
      ),
    );
  }
}
```

- [ ] **Step 2: Create stub screens so router compiles**

Create `lib/features/home/home_screen.dart`:
```dart
import 'package:flutter/material.dart';
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('首页')));
}
```

Create `lib/features/calendar/calendar_screen.dart`:
```dart
import 'package:flutter/material.dart';
class CalendarScreen extends StatelessWidget {
  const CalendarScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('日历')));
}
```

Create `lib/features/calendar/create_event_screen.dart`:
```dart
import 'package:flutter/material.dart';
class CreateEventScreen extends StatelessWidget {
  const CreateEventScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('创建事件')));
}
```

Create `lib/features/chat/session_list_screen.dart`:
```dart
import 'package:flutter/material.dart';
class SessionListScreen extends StatelessWidget {
  const SessionListScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('会话列表')));
}
```

Create `lib/features/chat/chat_screen.dart`:
```dart
import 'package:flutter/material.dart';
class ChatScreen extends StatelessWidget {
  final int sessionId;
  const ChatScreen({super.key, required this.sessionId});
  @override
  Widget build(BuildContext context) => Scaffold(body: Center(child: Text('聊天 $sessionId')));
}
```

Create `lib/features/biography/biography_chat_screen.dart`:
```dart
import 'package:flutter/material.dart';
class BiographyChatScreen extends StatelessWidget {
  const BiographyChatScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('传记聊天')));
}
```

Create `lib/features/biography/biography_timeline_screen.dart`:
```dart
import 'package:flutter/material.dart';
class BiographyTimelineScreen extends StatelessWidget {
  const BiographyTimelineScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('人生时间轴')));
}
```

Create `lib/features/search/search_screen.dart`:
```dart
import 'package:flutter/material.dart';
class SearchScreen extends StatelessWidget {
  const SearchScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('搜索')));
}
```

Create `lib/features/profile/profile_screen.dart`:
```dart
import 'package:flutter/material.dart';
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(body: Center(child: Text('我的')));
}
```

- [ ] **Step 3: Run analysis**

```bash
flutter analyze
```

Expected: No errors.

- [ ] **Step 4: Commit**

```bash
git add lib/router/ lib/features/
git commit -m "feat: add app shell with go_router, bottom navigation, and screen stubs"
```

---

### Task 6: Home Dashboard Screen

**Files:**
- Create: `lib/features/home/home_repository.dart`
- Create: `lib/features/home/home_provider.dart`
- Modify: `lib/features/home/home_screen.dart`

- [ ] **Step 1: Create lib/features/home/home_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../models/calendar_event.dart';
import '../../models/chat_session.dart';
import '../../models/learning_session.dart';

class HomeRepository {
  final ApiClient _apiClient;
  HomeRepository({required ApiClient apiClient}) : _apiClient = apiClient;

  Future<String> fetchNewsDigest() async {
    final response = await _apiClient.get<String>(
      '/api/news/today',
      (d) => d as String,
    );
    return response.data ?? '';
  }

  Future<List<CalendarEvent>> fetchTodayEvents() async {
    final response = await _apiClient.get<List<CalendarEvent>>(
      '/api/calendar/events',
      (d) => (d as List).map((e) => CalendarEvent.fromJson(e as Map<String, dynamic>)).toList(),
    );
    return response.data ?? [];
  }

  Future<List<ChatSession>> fetchRecentSessions() async {
    final response = await _apiClient.get<List<ChatSession>>(
      '/api/chat/sessions',
      (d) => (d as List).map((e) => ChatSession.fromJson(e as Map<String, dynamic>)).toList(),
    );
    return (response.data ?? []).take(3).toList();
  }

  Future<List<LearningSession>> fetchRecentLearning() async {
    final response = await _apiClient.get<List<LearningSession>>(
      '/api/learning/sessions',
      (d) => (d as List).map((e) => LearningSession.fromJson(e as Map<String, dynamic>)).toList(),
    );
    return (response.data ?? []).take(3).toList();
  }
}
```

- [ ] **Step 2: Create lib/features/home/home_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import '../../models/calendar_event.dart';
import '../../models/chat_session.dart';
import '../../models/learning_session.dart';
import 'home_repository.dart';

final homeRepositoryProvider = Provider<HomeRepository>(
  (ref) => HomeRepository(apiClient: ref.watch(apiClientProvider)),
);

final newsDigestProvider = FutureProvider<String>((ref) async {
  return ref.watch(homeRepositoryProvider).fetchNewsDigest();
});

final todayEventsProvider = FutureProvider<List<CalendarEvent>>((ref) async {
  return ref.watch(homeRepositoryProvider).fetchTodayEvents();
});

final recentSessionsProvider = FutureProvider<List<ChatSession>>((ref) async {
  return ref.watch(homeRepositoryProvider).fetchRecentSessions();
});

final recentLearningProvider = FutureProvider<List<LearningSession>>((ref) async {
  return ref.watch(homeRepositoryProvider).fetchRecentLearning();
});
```

- [ ] **Step 3: Replace lib/features/home/home_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'home_provider.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final news = ref.watch(newsDigestProvider);
    final events = ref.watch(todayEventsProvider);
    final sessions = ref.watch(recentSessionsProvider);
    final learning = ref.watch(recentLearningProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('首页'), centerTitle: true),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(newsDigestProvider);
          ref.invalidate(todayEventsProvider);
          ref.invalidate(recentSessionsProvider);
          ref.invalidate(recentLearningProvider);
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _SectionTitle(title: '今日新闻摘要', onMore: null),
            news.when(
              data: (text) => Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Text(text.isEmpty ? '暂无新闻' : text,
                      maxLines: 5, overflow: TextOverflow.ellipsis),
                ),
              ),
              loading: () => const LinearProgressIndicator(),
              error: (e, _) => Text('加载失败: $e'),
            ),
            const SizedBox(height: 16),
            _SectionTitle(title: '今日待办', onMore: () => context.go('/calendar')),
            events.when(
              data: (list) => list.isEmpty
                  ? const Text('今天没有事项')
                  : Column(
                      children: list
                          .map((e) => ListTile(
                                leading: const Icon(Icons.event),
                                title: Text(e.title),
                                dense: true,
                              ))
                          .toList(),
                    ),
              loading: () => const LinearProgressIndicator(),
              error: (e, _) => Text('加载失败: $e'),
            ),
            const SizedBox(height: 16),
            _SectionTitle(title: '最近聊天', onMore: () => context.go('/chat')),
            sessions.when(
              data: (list) => list.isEmpty
                  ? const Text('暂无聊天记录')
                  : Column(
                      children: list
                          .map((s) => ListTile(
                                leading: const Icon(Icons.chat_bubble_outline),
                                title: Text(s.title),
                                dense: true,
                                onTap: () => context.push('/chat/${s.id}'),
                              ))
                          .toList(),
                    ),
              loading: () => const LinearProgressIndicator(),
              error: (e, _) => Text('加载失败: $e'),
            ),
            const SizedBox(height: 16),
            _SectionTitle(title: '学习进度', onMore: () => context.go('/chat')),
            learning.when(
              data: (list) => list.isEmpty
                  ? const Text('暂无学习记录')
                  : Column(
                      children: list
                          .map((l) => ListTile(
                                leading: const Icon(Icons.school_outlined),
                                title: Text('${l.subject} · ${l.topic}'),
                                trailing: l.score != null ? Text('${l.score}分') : null,
                                dense: true,
                              ))
                          .toList(),
                    ),
              loading: () => const LinearProgressIndicator(),
              error: (e, _) => Text('加载失败: $e'),
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  final String title;
  final VoidCallback? onMore;
  const _SectionTitle({required this.title, this.onMore});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          if (onMore != null)
            TextButton(onPressed: onMore, child: const Text('更多')),
        ],
      ),
    );
  }
}
```

- [ ] **Step 4: Run analysis**

```bash
flutter analyze lib/features/home/
```

Expected: No errors.

- [ ] **Step 5: Commit**

```bash
git add lib/features/home/
git commit -m "feat: implement home dashboard screen with news, events, sessions"
```

---

### Task 7: Calendar Feature

**Files:**
- Create: `lib/features/calendar/calendar_repository.dart`
- Create: `lib/features/calendar/calendar_provider.dart`
- Modify: `lib/features/calendar/calendar_screen.dart`
- Modify: `lib/features/calendar/create_event_screen.dart`
- Create: `test/features/calendar/calendar_repository_test.dart`

- [ ] **Step 1: Write failing test**

Create `test/features/calendar/calendar_repository_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:personal_ai_assistant/core/api_client.dart';
import 'package:personal_ai_assistant/core/api_response.dart';
import 'package:personal_ai_assistant/features/calendar/calendar_repository.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockApiClient mockClient;
  late CalendarRepository repo;

  setUp(() {
    mockClient = MockApiClient();
    repo = CalendarRepository(apiClient: mockClient);
  });

  test('fetchEvents returns list of events', () async {
    when(() => mockClient.get<List>(any(), any())).thenAnswer((_) async =>
        const ApiResponse(success: true, message: 'OK', data: []));

    final result = await repo.fetchEvents();
    expect(result, isEmpty);
  });

  test('createEvent calls post with correct path', () async {
    when(() => mockClient.post<void>(any(), any(), any()))
        .thenAnswer((_) async => const ApiResponse(success: true, message: 'OK'));

    await repo.createEvent(
      title: '开会',
      startTime: DateTime(2026, 6, 1, 14),
    );

    verify(() => mockClient.post<void>(
          '/api/calendar/events',
          any(),
          any(),
        )).called(1);
  });
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
flutter test test/features/calendar/calendar_repository_test.dart
```

Expected: FAIL.

- [ ] **Step 3: Create lib/features/calendar/calendar_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../models/calendar_event.dart';

class CalendarRepository {
  final ApiClient _apiClient;
  CalendarRepository({required ApiClient apiClient}) : _apiClient = apiClient;

  Future<List<CalendarEvent>> fetchEvents() async {
    final response = await _apiClient.get<List<CalendarEvent>>(
      '/api/calendar/events',
      (d) => (d as List)
          .map((e) => CalendarEvent.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return response.data ?? [];
  }

  Future<void> createEvent({
    required String title,
    required DateTime startTime,
    DateTime? endTime,
    DateTime? remindAt,
  }) async {
    await _apiClient.post<void>(
      '/api/calendar/events',
      {
        'title': title,
        'startTime': startTime.toIso8601String(),
        if (endTime != null) 'endTime': endTime.toIso8601String(),
        if (remindAt != null) 'remindAt': remindAt.toIso8601String(),
      },
      (_) {},
    );
  }

  Future<void> deleteEvent(int id) async {
    await _apiClient.post<void>('/api/calendar/events/$id/delete', {}, (_) {});
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
flutter test test/features/calendar/calendar_repository_test.dart
```

Expected: PASS — 2 tests pass.

- [ ] **Step 5: Create lib/features/calendar/calendar_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import '../../models/calendar_event.dart';
import 'calendar_repository.dart';

final calendarRepositoryProvider = Provider<CalendarRepository>(
  (ref) => CalendarRepository(apiClient: ref.watch(apiClientProvider)),
);

final calendarEventsProvider = FutureProvider<List<CalendarEvent>>((ref) async {
  return ref.watch(calendarRepositoryProvider).fetchEvents();
});

class CalendarNotifier extends AsyncNotifier<void> {
  @override
  Future<void> build() async {}

  Future<void> createEvent({
    required String title,
    required DateTime startTime,
    DateTime? endTime,
  }) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() =>
        ref.read(calendarRepositoryProvider).createEvent(
              title: title,
              startTime: startTime,
              endTime: endTime,
            ));
    ref.invalidate(calendarEventsProvider);
  }
}

final calendarNotifierProvider =
    AsyncNotifierProvider<CalendarNotifier, void>(CalendarNotifier.new);
```

- [ ] **Step 6: Replace lib/features/calendar/calendar_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'calendar_provider.dart';

class CalendarScreen extends ConsumerWidget {
  const CalendarScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final eventsAsync = ref.watch(calendarEventsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('日历'), centerTitle: true),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/calendar/create'),
        child: const Icon(Icons.add),
      ),
      body: RefreshIndicator(
        onRefresh: () async => ref.invalidate(calendarEventsProvider),
        child: eventsAsync.when(
          data: (events) => events.isEmpty
              ? const Center(child: Text('暂无事件，点击 + 创建'))
              : ListView.builder(
                  itemCount: events.length,
                  itemBuilder: (ctx, i) {
                    final e = events[i];
                    return ListTile(
                      leading: const Icon(Icons.event),
                      title: Text(e.title),
                      subtitle: Text(DateFormat('yyyy-MM-dd HH:mm').format(e.startTime)),
                    );
                  },
                ),
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('加载失败: $e')),
        ),
      ),
    );
  }
}
```

- [ ] **Step 7: Replace lib/features/calendar/create_event_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'calendar_provider.dart';

class CreateEventScreen extends ConsumerStatefulWidget {
  const CreateEventScreen({super.key});

  @override
  ConsumerState<CreateEventScreen> createState() => _CreateEventScreenState();
}

class _CreateEventScreenState extends ConsumerState<CreateEventScreen> {
  final _titleCtrl = TextEditingController();
  DateTime _startTime = DateTime.now().add(const Duration(hours: 1));
  String? _error;
  bool _loading = false;

  @override
  void dispose() {
    _titleCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (_titleCtrl.text.trim().isEmpty) {
      setState(() => _error = '请输入事件标题');
      return;
    }
    setState(() { _loading = true; _error = null; });
    try {
      await ref.read(calendarNotifierProvider.notifier).createEvent(
            title: _titleCtrl.text.trim(),
            startTime: _startTime,
          );
      if (mounted) context.pop();
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _pickDateTime() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _startTime,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_startTime),
    );
    if (time == null) return;
    setState(() => _startTime = DateTime(date.year, date.month, date.day, time.hour, time.minute));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('创建事件')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            if (_error != null) Text(_error!, style: const TextStyle(color: Colors.red)),
            TextField(
              controller: _titleCtrl,
              decoration: const InputDecoration(labelText: '事件标题', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.access_time),
              title: Text('开始时间：${DateFormat('yyyy-MM-dd HH:mm').format(_startTime)}'),
              onTap: _pickDateTime,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: const BorderSide(color: Colors.grey),
              ),
            ),
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: _loading ? null : _submit,
                child: _loading ? const CircularProgressIndicator(color: Colors.white) : const Text('创建'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
```

- [ ] **Step 8: Commit**

```bash
git add lib/features/calendar/ test/features/calendar/
git commit -m "feat: implement calendar screen with event list and create form"
```

---

### Task 8: Chat Feature

**Files:**
- Create: `lib/features/chat/chat_repository.dart`
- Create: `lib/features/chat/chat_provider.dart`
- Modify: `lib/features/chat/session_list_screen.dart`
- Modify: `lib/features/chat/chat_screen.dart`
- Create: `test/features/chat/chat_repository_test.dart`

- [ ] **Step 1: Write failing test**

Create `test/features/chat/chat_repository_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:personal_ai_assistant/core/api_client.dart';
import 'package:personal_ai_assistant/core/api_response.dart';
import 'package:personal_ai_assistant/features/chat/chat_repository.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockApiClient mockClient;
  late ChatRepository repo;

  setUp(() {
    mockClient = MockApiClient();
    repo = ChatRepository(apiClient: mockClient);
  });

  test('sendMessage returns reply and sessionId', () async {
    when(() => mockClient.post<Map<String, dynamic>>(any(), any(), any()))
        .thenAnswer((_) async => const ApiResponse(
              success: true,
              message: 'OK',
              data: {'reply': 'Hello!', 'sessionId': 42},
            ));

    final result = await repo.sendMessage(message: 'Hi', sessionId: null);
    expect(result.reply, equals('Hello!'));
    expect(result.sessionId, equals(42));
  });
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
flutter test test/features/chat/chat_repository_test.dart
```

Expected: FAIL.

- [ ] **Step 3: Create lib/features/chat/chat_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../models/chat_session.dart';
import '../../models/chat_message.dart';

class ChatReply {
  final String reply;
  final int sessionId;
  const ChatReply({required this.reply, required this.sessionId});
}

class ChatRepository {
  final ApiClient _apiClient;
  ChatRepository({required ApiClient apiClient}) : _apiClient = apiClient;

  Future<List<ChatSession>> fetchSessions() async {
    final response = await _apiClient.get<List<ChatSession>>(
      '/api/chat/sessions',
      (d) => (d as List)
          .map((e) => ChatSession.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return response.data ?? [];
  }

  Future<List<ChatMessage>> fetchMessages(int sessionId) async {
    final response = await _apiClient.get<List<ChatMessage>>(
      '/api/chat/sessions/$sessionId/messages',
      (d) => (d as List)
          .map((e) => ChatMessage.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return response.data ?? [];
  }

  Future<ChatReply> sendMessage({
    required String message,
    required int? sessionId,
  }) async {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/chat',
      {
        'message': message,
        if (sessionId != null) 'sessionId': sessionId,
      },
      (d) => d as Map<String, dynamic>,
    );
    final data = response.data!;
    return ChatReply(
      reply: data['reply'] as String,
      sessionId: data['sessionId'] as int,
    );
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
flutter test test/features/chat/chat_repository_test.dart
```

Expected: PASS.

- [ ] **Step 5: Create lib/features/chat/chat_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import '../../models/chat_message.dart';
import '../../models/chat_session.dart';
import 'chat_repository.dart';

final chatRepositoryProvider = Provider<ChatRepository>(
  (ref) => ChatRepository(apiClient: ref.watch(apiClientProvider)),
);

final chatSessionsProvider = FutureProvider<List<ChatSession>>((ref) async {
  return ref.watch(chatRepositoryProvider).fetchSessions();
});

// Messages for a specific session
final chatMessagesProvider =
    FutureProvider.family<List<ChatMessage>, int>((ref, sessionId) async {
  return ref.watch(chatRepositoryProvider).fetchMessages(sessionId);
});

// Notifier that manages chat state for one session
class ChatNotifier extends FamilyAsyncNotifier<List<ChatMessage>, int?> {
  @override
  Future<List<ChatMessage>> build(int? arg) async {
    if (arg == null) return [];
    return ref.read(chatRepositoryProvider).fetchMessages(arg);
  }

  int? _sessionId;

  int? get currentSessionId => _sessionId ?? arg;

  Future<void> send(String message) async {
    final previousMessages = state.valueOrNull ?? [];
    final userMsg = ChatMessage(
      id: -1,
      role: 'user',
      content: message,
      createdAt: DateTime.now(),
    );
    state = AsyncData([...previousMessages, userMsg]);

    try {
      final reply = await ref
          .read(chatRepositoryProvider)
          .sendMessage(message: message, sessionId: currentSessionId);
      _sessionId = reply.sessionId;
      final assistantMsg = ChatMessage(
        id: -2,
        role: 'assistant',
        content: reply.reply,
        createdAt: DateTime.now(),
      );
      state = AsyncData([...previousMessages, userMsg, assistantMsg]);
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }
}

final chatNotifierProvider =
    AsyncNotifierProviderFamily<ChatNotifier, List<ChatMessage>, int?>(
        ChatNotifier.new);
```

- [ ] **Step 6: Replace lib/features/chat/session_list_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'chat_provider.dart';

class SessionListScreen extends ConsumerWidget {
  const SessionListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessionsAsync = ref.watch(chatSessionsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('聊天'), centerTitle: true),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/chat/new'),
        child: const Icon(Icons.add_comment_outlined),
      ),
      body: Column(
        children: [
          // Navigation to biography and learning
          Padding(
            padding: const EdgeInsets.all(8),
            child: Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.auto_stories),
                    label: const Text('传记模式'),
                    onPressed: () => context.push('/biography'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.school),
                    label: const Text('学习陪练'),
                    onPressed: () => context.push('/chat/learning'),
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () async => ref.invalidate(chatSessionsProvider),
              child: sessionsAsync.when(
                data: (sessions) => sessions.isEmpty
                    ? const Center(child: Text('暂无聊天记录'))
                    : ListView.builder(
                        itemCount: sessions.length,
                        itemBuilder: (ctx, i) {
                          final s = sessions[i];
                          return ListTile(
                            leading: const Icon(Icons.chat_bubble_outline),
                            title: Text(s.title),
                            subtitle: Text(s.mode),
                            onTap: () => context.push('/chat/${s.id}'),
                          );
                        },
                      ),
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (e, _) => Center(child: Text('加载失败: $e')),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
```

- [ ] **Step 7: Replace lib/features/chat/chat_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'chat_provider.dart';

class ChatScreen extends ConsumerStatefulWidget {
  final int? sessionId; // null = new chat
  const ChatScreen({super.key, this.sessionId});

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final _ctrl = TextEditingController();
  final _scrollCtrl = ScrollController();
  bool _sending = false;

  @override
  void dispose() {
    _ctrl.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final text = _ctrl.text.trim();
    if (text.isEmpty) return;
    _ctrl.clear();
    setState(() => _sending = true);
    try {
      await ref.read(chatNotifierProvider(widget.sessionId).notifier).send(text);
      _scrollToBottom();
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.animateTo(
          _scrollCtrl.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final messagesAsync = ref.watch(chatNotifierProvider(widget.sessionId));

    return Scaffold(
      appBar: AppBar(title: const Text('聊天')),
      body: Column(
        children: [
          Expanded(
            child: messagesAsync.when(
              data: (messages) => ListView.builder(
                controller: _scrollCtrl,
                padding: const EdgeInsets.all(12),
                itemCount: messages.length,
                itemBuilder: (ctx, i) => _MessageBubble(message: messages[i]),
              ),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('错误: $e')),
            ),
          ),
          if (_sending) const LinearProgressIndicator(),
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 4, 8, 16),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _ctrl,
                    minLines: 1,
                    maxLines: 4,
                    decoration: const InputDecoration(
                      hintText: '输入消息...',
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    textInputAction: TextInputAction.send,
                    onSubmitted: (_) => _send(),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  icon: const Icon(Icons.send),
                  onPressed: _sending ? null : _send,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _MessageBubble extends StatelessWidget {
  final dynamic message;
  const _MessageBubble({required this.message});

  @override
  Widget build(BuildContext context) {
    final isUser = message.role == 'user';
    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.75),
        decoration: BoxDecoration(
          color: isUser
              ? Theme.of(context).colorScheme.primary
              : Theme.of(context).colorScheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Text(
          message.content as String,
          style: TextStyle(
            color: isUser ? Colors.white : Theme.of(context).colorScheme.onSurface,
          ),
        ),
      ),
    );
  }
}
```

- [ ] **Step 8: Update router to handle new chat (null sessionId)**

In `lib/router/app_router.dart`, update the chat routes to handle new sessions:

```dart
GoRoute(
  path: '/chat',
  builder: (_, __) => const SessionListScreen(),
  routes: [
    GoRoute(
      path: 'new',
      builder: (_, __) => const ChatScreen(sessionId: null),
    ),
    GoRoute(
      path: 'learning',
      builder: (_, __) => const LearningScreen(),
    ),
    GoRoute(
      path: ':sessionId',
      builder: (_, state) => ChatScreen(
        sessionId: int.tryParse(state.pathParameters['sessionId']!),
      ),
    ),
  ],
),
```

Also add the import for LearningScreen:
```dart
import '../features/learning/learning_screen.dart';
```

- [ ] **Step 9: Commit**

```bash
git add lib/features/chat/ test/features/chat/ lib/router/
git commit -m "feat: implement chat sessions list and chat screen with Riverpod"
```

---

### Task 9: Biography Feature

**Files:**
- Create: `lib/features/biography/biography_repository.dart`
- Create: `lib/features/biography/biography_provider.dart`
- Modify: `lib/features/biography/biography_chat_screen.dart`
- Modify: `lib/features/biography/biography_timeline_screen.dart`
- Create: `test/features/biography/biography_repository_test.dart`

- [ ] **Step 1: Write failing test**

Create `test/features/biography/biography_repository_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:personal_ai_assistant/core/api_client.dart';
import 'package:personal_ai_assistant/core/api_response.dart';
import 'package:personal_ai_assistant/features/biography/biography_repository.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockApiClient mockClient;
  late BiographyRepository repo;

  setUp(() {
    mockClient = MockApiClient();
    repo = BiographyRepository(apiClient: mockClient);
  });

  test('sendBiographyMessage returns reply and events', () async {
    when(() => mockClient.post<Map<String, dynamic>>(any(), any(), any()))
        .thenAnswer((_) async => const ApiResponse(
              success: true,
              message: 'OK',
              data: {
                'reply': '很精彩的故事！',
                'sessionId': 1,
                'newEvents': [],
              },
            ));

    final result = await repo.sendMessage(message: '我1998年上了大学', sessionId: null);
    expect(result.reply, equals('很精彩的故事！'));
  });
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
flutter test test/features/biography/biography_repository_test.dart
```

Expected: FAIL.

- [ ] **Step 3: Create lib/features/biography/biography_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../models/biography_event.dart';

class BiographyChatResult {
  final String reply;
  final int sessionId;
  final List<BiographyEvent> newEvents;

  const BiographyChatResult({
    required this.reply,
    required this.sessionId,
    required this.newEvents,
  });
}

class BiographyRepository {
  final ApiClient _apiClient;
  BiographyRepository({required ApiClient apiClient}) : _apiClient = apiClient;

  Future<BiographyChatResult> sendMessage({
    required String message,
    required int? sessionId,
  }) async {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/biography/chat',
      {
        'message': message,
        if (sessionId != null) 'sessionId': sessionId,
      },
      (d) => d as Map<String, dynamic>,
    );
    final data = response.data!;
    final rawEvents = data['newEvents'] as List? ?? [];
    return BiographyChatResult(
      reply: data['reply'] as String,
      sessionId: data['sessionId'] as int,
      newEvents: rawEvents
          .map((e) => BiographyEvent.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }

  Future<List<BiographyEvent>> fetchEvents() async {
    final response = await _apiClient.get<List<BiographyEvent>>(
      '/api/biography/events',
      (d) => (d as List)
          .map((e) => BiographyEvent.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return response.data ?? [];
  }

  Future<String> generateBiography() async {
    final response = await _apiClient.post<String>(
      '/api/biography/generate',
      {},
      (d) => d as String,
    );
    return response.data ?? '';
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
flutter test test/features/biography/biography_repository_test.dart
```

Expected: PASS.

- [ ] **Step 5: Create lib/features/biography/biography_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import '../../models/biography_event.dart';
import '../../models/chat_message.dart';
import 'biography_repository.dart';

final biographyRepositoryProvider = Provider<BiographyRepository>(
  (ref) => BiographyRepository(apiClient: ref.watch(apiClientProvider)),
);

final biographyEventsProvider = FutureProvider<List<BiographyEvent>>((ref) async {
  return ref.watch(biographyRepositoryProvider).fetchEvents();
});

class BiographyChatNotifier extends AsyncNotifier<List<ChatMessage>> {
  int? _sessionId;

  @override
  Future<List<ChatMessage>> build() async => [];

  Future<void> send(String message) async {
    final previous = state.valueOrNull ?? [];
    final userMsg = ChatMessage(
      id: -1, role: 'user', content: message, createdAt: DateTime.now(),
    );
    state = AsyncData([...previous, userMsg]);

    try {
      final result = await ref
          .read(biographyRepositoryProvider)
          .sendMessage(message: message, sessionId: _sessionId);
      _sessionId = result.sessionId;
      final assistantMsg = ChatMessage(
        id: -2, role: 'assistant', content: result.reply, createdAt: DateTime.now(),
      );
      state = AsyncData([...previous, userMsg, assistantMsg]);
      if (result.newEvents.isNotEmpty) {
        ref.invalidate(biographyEventsProvider);
      }
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }
}

final biographyChatNotifierProvider =
    AsyncNotifierProvider<BiographyChatNotifier, List<ChatMessage>>(
        BiographyChatNotifier.new);
```

- [ ] **Step 6: Replace lib/features/biography/biography_chat_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/chat_message.dart';
import 'biography_provider.dart';

class BiographyChatScreen extends ConsumerStatefulWidget {
  const BiographyChatScreen({super.key});

  @override
  ConsumerState<BiographyChatScreen> createState() => _BiographyChatScreenState();
}

class _BiographyChatScreenState extends ConsumerState<BiographyChatScreen> {
  final _ctrl = TextEditingController();
  final _scrollCtrl = ScrollController();
  bool _sending = false;

  @override
  void dispose() {
    _ctrl.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final text = _ctrl.text.trim();
    if (text.isEmpty) return;
    _ctrl.clear();
    setState(() => _sending = true);
    try {
      await ref.read(biographyChatNotifierProvider.notifier).send(text);
      _scrollToBottom();
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.animateTo(
          _scrollCtrl.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final messagesAsync = ref.watch(biographyChatNotifierProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('传记模式'),
        actions: [
          IconButton(
            icon: const Icon(Icons.timeline),
            tooltip: '人生时间轴',
            onPressed: () => context.push('/biography/timeline'),
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(8),
            color: Theme.of(context).colorScheme.primaryContainer,
            child: const Text(
              '传记模式：AI 将引导您讲述故事，并自动提取人生事件。',
              style: TextStyle(fontSize: 12),
              textAlign: TextAlign.center,
            ),
          ),
          Expanded(
            child: messagesAsync.when(
              data: (messages) => messages.isEmpty
                  ? const Center(child: Text('开始讲述您的故事吧...'))
                  : ListView.builder(
                      controller: _scrollCtrl,
                      padding: const EdgeInsets.all(12),
                      itemCount: messages.length,
                      itemBuilder: (ctx, i) => _BioBubble(message: messages[i]),
                    ),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('错误: $e')),
            ),
          ),
          if (_sending) const LinearProgressIndicator(),
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 4, 8, 16),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _ctrl,
                    minLines: 1,
                    maxLines: 4,
                    decoration: const InputDecoration(
                      hintText: '分享您的故事...',
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    textInputAction: TextInputAction.send,
                    onSubmitted: (_) => _send(),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  icon: const Icon(Icons.send),
                  onPressed: _sending ? null : _send,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _BioBubble extends StatelessWidget {
  final ChatMessage message;
  const _BioBubble({required this.message});

  @override
  Widget build(BuildContext context) {
    final isUser = message.role == 'user';
    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.75),
        decoration: BoxDecoration(
          color: isUser
              ? Theme.of(context).colorScheme.primary
              : Theme.of(context).colorScheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Text(
          message.content,
          style: TextStyle(
            color: isUser ? Colors.white : Theme.of(context).colorScheme.onSurface,
          ),
        ),
      ),
    );
  }
}
```

- [ ] **Step 7: Replace lib/features/biography/biography_timeline_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'biography_provider.dart';

class BiographyTimelineScreen extends ConsumerWidget {
  const BiographyTimelineScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final eventsAsync = ref.watch(biographyEventsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('人生时间轴')),
      body: RefreshIndicator(
        onRefresh: () async => ref.invalidate(biographyEventsProvider),
        child: eventsAsync.when(
          data: (events) => events.isEmpty
              ? const Center(child: Text('暂无传记事件，开始讲述您的故事吧'))
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: events.length,
                  itemBuilder: (ctx, i) {
                    final e = events[i];
                    return Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Column(
                          children: [
                            Container(
                              width: 12,
                              height: 12,
                              decoration: BoxDecoration(
                                color: Theme.of(context).colorScheme.primary,
                                shape: BoxShape.circle,
                              ),
                            ),
                            if (i < events.length - 1)
                              Container(
                                width: 2,
                                height: 80,
                                color: Theme.of(context).colorScheme.outlineVariant,
                              ),
                          ],
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Padding(
                            padding: const EdgeInsets.only(bottom: 16),
                            child: Card(
                              child: Padding(
                                padding: const EdgeInsets.all(12),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        Text(
                                          e.eventDate,
                                          style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                                color: Theme.of(context).colorScheme.primary,
                                                fontWeight: FontWeight.bold,
                                              ),
                                        ),
                                        const SizedBox(width: 8),
                                        Chip(
                                          label: Text(e.category, style: const TextStyle(fontSize: 10)),
                                          padding: EdgeInsets.zero,
                                          visualDensity: VisualDensity.compact,
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 4),
                                    Text(e.title, style: Theme.of(context).textTheme.titleSmall),
                                    const SizedBox(height: 4),
                                    Text(e.content, style: Theme.of(context).textTheme.bodySmall, maxLines: 3, overflow: TextOverflow.ellipsis),
                                  ],
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    );
                  },
                ),
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('加载失败: $e')),
        ),
      ),
    );
  }
}
```

- [ ] **Step 8: Commit**

```bash
git add lib/features/biography/ test/features/biography/
git commit -m "feat: implement biography chat and timeline screens"
```

---

### Task 10: Search Feature

**Files:**
- Create: `lib/features/search/search_repository.dart`
- Create: `lib/features/search/search_provider.dart`
- Modify: `lib/features/search/search_screen.dart`
- Create: `test/features/search/search_repository_test.dart`

- [ ] **Step 1: Write failing test**

Create `test/features/search/search_repository_test.dart`:

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:personal_ai_assistant/core/api_client.dart';
import 'package:personal_ai_assistant/core/api_response.dart';
import 'package:personal_ai_assistant/features/search/search_repository.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockApiClient mockClient;
  late SearchRepository repo;

  setUp(() {
    mockClient = MockApiClient();
    repo = SearchRepository(apiClient: mockClient);
  });

  test('search returns summary and sources', () async {
    when(() => mockClient.post<Map<String, dynamic>>(any(), any(), any()))
        .thenAnswer((_) async => const ApiResponse(
              success: true,
              message: 'OK',
              data: {'summary': 'Test summary', 'sources': []},
            ));

    final result = await repo.search('Flutter');
    expect(result.summary, equals('Test summary'));
    expect(result.sources, isEmpty);
  });
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
flutter test test/features/search/search_repository_test.dart
```

Expected: FAIL.

- [ ] **Step 3: Create lib/features/search/search_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../models/search_history.dart';

class SearchResult {
  final String summary;
  final List<Map<String, dynamic>> sources;
  final int historyId;

  const SearchResult({
    required this.summary,
    required this.sources,
    required this.historyId,
  });
}

class SearchRepository {
  final ApiClient _apiClient;
  SearchRepository({required ApiClient apiClient}) : _apiClient = apiClient;

  Future<SearchResult> search(String query) async {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/search',
      {'query': query},
      (d) => d as Map<String, dynamic>,
    );
    final data = response.data!;
    final rawSources = data['sources'] as List? ?? [];
    return SearchResult(
      summary: data['summary'] as String? ?? '',
      sources: rawSources.cast<Map<String, dynamic>>(),
      historyId: data['id'] as int? ?? -1,
    );
  }

  Future<List<SearchHistory>> fetchHistory() async {
    final response = await _apiClient.get<List<SearchHistory>>(
      '/api/search/history',
      (d) => (d as List)
          .map((e) => SearchHistory.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return response.data ?? [];
  }

  Future<void> starHistory(int id) async {
    await _apiClient.put<void>('/api/search/history/$id/star', {}, (_) {});
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
flutter test test/features/search/search_repository_test.dart
```

Expected: PASS.

- [ ] **Step 5: Create lib/features/search/search_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import '../../models/search_history.dart';
import 'search_repository.dart';

final searchRepositoryProvider = Provider<SearchRepository>(
  (ref) => SearchRepository(apiClient: ref.watch(apiClientProvider)),
);

final searchHistoryProvider = FutureProvider<List<SearchHistory>>((ref) async {
  return ref.watch(searchRepositoryProvider).fetchHistory();
});

class SearchState {
  final String? summary;
  final List<Map<String, dynamic>> sources;
  final bool loading;
  final String? error;

  const SearchState({
    this.summary,
    this.sources = const [],
    this.loading = false,
    this.error,
  });

  SearchState copyWith({
    String? summary,
    List<Map<String, dynamic>>? sources,
    bool? loading,
    String? error,
  }) =>
      SearchState(
        summary: summary ?? this.summary,
        sources: sources ?? this.sources,
        loading: loading ?? this.loading,
        error: error,
      );
}

class SearchNotifier extends Notifier<SearchState> {
  @override
  SearchState build() => const SearchState();

  Future<void> search(String query) async {
    state = state.copyWith(loading: true, error: null);
    try {
      final result = await ref.read(searchRepositoryProvider).search(query);
      state = state.copyWith(
        summary: result.summary,
        sources: result.sources,
        loading: false,
      );
      ref.invalidate(searchHistoryProvider);
    } catch (e) {
      state = state.copyWith(loading: false, error: e.toString());
    }
  }

  void clear() => state = const SearchState();
}

final searchNotifierProvider =
    NotifierProvider<SearchNotifier, SearchState>(SearchNotifier.new);
```

- [ ] **Step 6: Replace lib/features/search/search_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'search_provider.dart';

class SearchScreen extends ConsumerStatefulWidget {
  const SearchScreen({super.key});

  @override
  ConsumerState<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends ConsumerState<SearchScreen> {
  final _ctrl = TextEditingController();

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final searchState = ref.watch(searchNotifierProvider);
    final historyAsync = ref.watch(searchHistoryProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('实时搜索'), centerTitle: true),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _ctrl,
                    decoration: InputDecoration(
                      hintText: '搜索任何问题...',
                      border: const OutlineInputBorder(),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      suffixIcon: _ctrl.text.isNotEmpty
                          ? IconButton(
                              icon: const Icon(Icons.clear),
                              onPressed: () {
                                _ctrl.clear();
                                ref.read(searchNotifierProvider.notifier).clear();
                              },
                            )
                          : null,
                    ),
                    onSubmitted: (q) {
                      if (q.trim().isNotEmpty) {
                        ref.read(searchNotifierProvider.notifier).search(q.trim());
                      }
                    },
                    onChanged: (_) => setState(() {}),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  icon: const Icon(Icons.search),
                  onPressed: searchState.loading
                      ? null
                      : () {
                          final q = _ctrl.text.trim();
                          if (q.isNotEmpty) {
                            ref.read(searchNotifierProvider.notifier).search(q);
                          }
                        },
                ),
              ],
            ),
          ),
          if (searchState.loading) const LinearProgressIndicator(),
          if (searchState.error != null)
            Padding(
              padding: const EdgeInsets.all(8),
              child: Text(searchState.error!, style: const TextStyle(color: Colors.red)),
            ),
          Expanded(
            child: searchState.summary != null
                ? _SearchResultView(state: searchState)
                : historyAsync.when(
                    data: (history) => history.isEmpty
                        ? const Center(child: Text('输入问题开始搜索'))
                        : ListView.builder(
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            itemCount: history.length,
                            itemBuilder: (ctx, i) {
                              final h = history[i];
                              return ListTile(
                                leading: Icon(
                                  h.starred ? Icons.star : Icons.history,
                                  color: h.starred ? Colors.amber : null,
                                ),
                                title: Text(h.query),
                                subtitle: Text(h.summary, maxLines: 1, overflow: TextOverflow.ellipsis),
                                trailing: IconButton(
                                  icon: Icon(h.starred ? Icons.star : Icons.star_border),
                                  onPressed: () async {
                                    await ref.read(searchRepositoryProvider).starHistory(h.id);
                                    ref.invalidate(searchHistoryProvider);
                                  },
                                ),
                                onTap: () {
                                  _ctrl.text = h.query;
                                  ref.read(searchNotifierProvider.notifier).search(h.query);
                                },
                              );
                            },
                          ),
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (e, _) => Center(child: Text('加载失败: $e')),
                  ),
          ),
        ],
      ),
    );
  }
}

class _SearchResultView extends StatelessWidget {
  final SearchState state;
  const _SearchResultView({required this.state});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('搜索结果', style: Theme.of(context).textTheme.titleMedium),
                const Divider(),
                Text(state.summary ?? ''),
              ],
            ),
          ),
        ),
        if (state.sources.isNotEmpty) ...[
          const SizedBox(height: 8),
          Text('来源', style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 4),
          ...state.sources.map((s) => Card(
                child: ListTile(
                  dense: true,
                  leading: const Icon(Icons.link, size: 16),
                  title: Text(s['title'] as String? ?? '', maxLines: 1, overflow: TextOverflow.ellipsis),
                  subtitle: Text(s['url'] as String? ?? '', maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 11)),
                ),
              )),
        ],
      ],
    );
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add lib/features/search/ test/features/search/
git commit -m "feat: implement search screen with results and history"
```

---

### Task 11: Learning Feature + Profile Screen

**Files:**
- Create: `lib/features/learning/learning_repository.dart`
- Create: `lib/features/learning/learning_provider.dart`
- Modify: `lib/features/learning/learning_screen.dart`
- Modify: `lib/features/profile/profile_screen.dart`

- [ ] **Step 1: Create lib/features/learning/learning_repository.dart**

```dart
import '../../core/api_client.dart';
import '../../models/learning_session.dart';

class LearningReply {
  final String reply;
  final int sessionId;
  const LearningReply({required this.reply, required this.sessionId});
}

class LearningRepository {
  final ApiClient _apiClient;
  LearningRepository({required ApiClient apiClient}) : _apiClient = apiClient;

  Future<List<LearningSession>> fetchSessions() async {
    final response = await _apiClient.get<List<LearningSession>>(
      '/api/learning/sessions',
      (d) => (d as List)
          .map((e) => LearningSession.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
    return response.data ?? [];
  }

  Future<LearningReply> sendMessage({
    required String message,
    required int? sessionId,
    required String subject,
    required String topic,
  }) async {
    final response = await _apiClient.post<Map<String, dynamic>>(
      '/api/learning/chat',
      {
        'message': message,
        if (sessionId != null) 'sessionId': sessionId,
        'subject': subject,
        'topic': topic,
      },
      (d) => d as Map<String, dynamic>,
    );
    final data = response.data!;
    return LearningReply(
      reply: data['reply'] as String,
      sessionId: data['sessionId'] as int,
    );
  }
}
```

- [ ] **Step 2: Create lib/features/learning/learning_provider.dart**

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/providers.dart';
import '../../models/chat_message.dart';
import '../../models/learning_session.dart';
import 'learning_repository.dart';

final learningRepositoryProvider = Provider<LearningRepository>(
  (ref) => LearningRepository(apiClient: ref.watch(apiClientProvider)),
);

final learningSessionsProvider = FutureProvider<List<LearningSession>>((ref) async {
  return ref.watch(learningRepositoryProvider).fetchSessions();
});

class LearningChatNotifier extends AsyncNotifier<List<ChatMessage>> {
  int? _sessionId;
  String _subject = '';
  String _topic = '';

  void setSubjectTopic(String subject, String topic) {
    _subject = subject;
    _topic = topic;
  }

  @override
  Future<List<ChatMessage>> build() async => [];

  Future<void> send(String message) async {
    final previous = state.valueOrNull ?? [];
    final userMsg = ChatMessage(
      id: -1, role: 'user', content: message, createdAt: DateTime.now(),
    );
    state = AsyncData([...previous, userMsg]);

    try {
      final result = await ref.read(learningRepositoryProvider).sendMessage(
            message: message,
            sessionId: _sessionId,
            subject: _subject,
            topic: _topic,
          );
      _sessionId = result.sessionId;
      final assistantMsg = ChatMessage(
        id: -2, role: 'assistant', content: result.reply, createdAt: DateTime.now(),
      );
      state = AsyncData([...previous, userMsg, assistantMsg]);
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }
}

final learningChatNotifierProvider =
    AsyncNotifierProvider<LearningChatNotifier, List<ChatMessage>>(
        LearningChatNotifier.new);
```

- [ ] **Step 3: Replace lib/features/learning/learning_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'learning_provider.dart';

class LearningScreen extends ConsumerStatefulWidget {
  const LearningScreen({super.key});

  @override
  ConsumerState<LearningScreen> createState() => _LearningScreenState();
}

class _LearningScreenState extends ConsumerState<LearningScreen> {
  final _msgCtrl = TextEditingController();
  final _scrollCtrl = ScrollController();
  final _subjectCtrl = TextEditingController(text: '英语');
  final _topicCtrl = TextEditingController(text: '口语练习');
  bool _sending = false;
  bool _started = false;

  @override
  void dispose() {
    _msgCtrl.dispose();
    _scrollCtrl.dispose();
    _subjectCtrl.dispose();
    _topicCtrl.dispose();
    super.dispose();
  }

  void _start() {
    ref.read(learningChatNotifierProvider.notifier)
        .setSubjectTopic(_subjectCtrl.text.trim(), _topicCtrl.text.trim());
    setState(() => _started = true);
    ref.read(learningChatNotifierProvider.notifier).send('开始学习');
  }

  Future<void> _send() async {
    final text = _msgCtrl.text.trim();
    if (text.isEmpty) return;
    _msgCtrl.clear();
    setState(() => _sending = true);
    try {
      await ref.read(learningChatNotifierProvider.notifier).send(text);
      _scrollToBottom();
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollCtrl.hasClients) {
        _scrollCtrl.animateTo(
          _scrollCtrl.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final messagesAsync = ref.watch(learningChatNotifierProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('学习陪练')),
      body: !_started
          ? Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text('设置学习主题', style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 24),
                  TextField(
                    controller: _subjectCtrl,
                    decoration: const InputDecoration(labelText: '科目（如：英语、Python）', border: OutlineInputBorder()),
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _topicCtrl,
                    decoration: const InputDecoration(labelText: '主题（如：口语练习、算法）', border: OutlineInputBorder()),
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: _start,
                      child: const Text('开始学习'),
                    ),
                  ),
                ],
              ),
            )
          : Column(
              children: [
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(8),
                  color: Theme.of(context).colorScheme.secondaryContainer,
                  child: Text(
                    '${_subjectCtrl.text} · ${_topicCtrl.text}',
                    style: const TextStyle(fontSize: 12),
                    textAlign: TextAlign.center,
                  ),
                ),
                Expanded(
                  child: messagesAsync.when(
                    data: (messages) => ListView.builder(
                      controller: _scrollCtrl,
                      padding: const EdgeInsets.all(12),
                      itemCount: messages.length,
                      itemBuilder: (ctx, i) {
                        final m = messages[i];
                        final isUser = m.role == 'user';
                        return Align(
                          alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
                          child: Container(
                            margin: const EdgeInsets.symmetric(vertical: 4),
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.75),
                            decoration: BoxDecoration(
                              color: isUser
                                  ? Theme.of(context).colorScheme.primary
                                  : Theme.of(context).colorScheme.surfaceContainerHighest,
                              borderRadius: BorderRadius.circular(16),
                            ),
                            child: Text(
                              m.content,
                              style: TextStyle(color: isUser ? Colors.white : null),
                            ),
                          ),
                        );
                      },
                    ),
                    loading: () => const Center(child: CircularProgressIndicator()),
                    error: (e, _) => Center(child: Text('错误: $e')),
                  ),
                ),
                if (_sending) const LinearProgressIndicator(),
                Padding(
                  padding: const EdgeInsets.fromLTRB(8, 4, 8, 16),
                  child: Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _msgCtrl,
                          minLines: 1,
                          maxLines: 4,
                          decoration: const InputDecoration(
                            hintText: '回答问题或提问...',
                            border: OutlineInputBorder(),
                            contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                          ),
                          onSubmitted: (_) => _send(),
                        ),
                      ),
                      const SizedBox(width: 8),
                      IconButton.filled(
                        icon: const Icon(Icons.send),
                        onPressed: _sending ? null : _send,
                      ),
                    ],
                  ),
                ),
              ],
            ),
    );
  }
}
```

- [ ] **Step 4: Replace lib/features/profile/profile_screen.dart**

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../auth/auth_provider.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('我的'), centerTitle: true),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 30,
                    backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                    child: const Icon(Icons.person, size: 30),
                  ),
                  const SizedBox(width: 16),
                  const Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('个人账户', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          ListTile(
            leading: const Icon(Icons.timeline),
            title: const Text('人生时间轴'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => context.push('/biography/timeline'),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          const Divider(),
          ListTile(
            leading: const Icon(Icons.school_outlined),
            title: const Text('学习记录'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => context.push('/chat/learning'),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          const Divider(),
          ListTile(
            leading: const Icon(Icons.auto_stories),
            title: const Text('生成传记'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => context.push('/biography'),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          const Divider(),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              icon: const Icon(Icons.logout),
              label: const Text('退出登录'),
              style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
              onPressed: () async {
                await ref.read(authNotifierProvider.notifier).logout();
                if (context.mounted) context.go('/login');
              },
            ),
          ),
        ],
      ),
    );
  }
}
```

- [ ] **Step 5: Run full analysis**

```bash
flutter analyze
```

Expected: No errors.

- [ ] **Step 6: Run all tests**

```bash
flutter test
```

Expected: All tests pass.

- [ ] **Step 7: Commit**

```bash
git add lib/features/learning/ lib/features/profile/
git commit -m "feat: implement learning session screen and profile screen with logout"
```

---

## Self-Review

### Spec Coverage

| Spec Requirement | Covered By |
|---|---|
| 5-tab navigation (首页/日历/聊天/搜索/我的) | Task 5 (AppShell + GoRouter) |
| JWT auth + login/register | Task 4 (AuthRepository + screens) |
| Calendar month view + create events | Task 7 (CalendarScreen + CreateEventScreen) |
| Chat with GPT, session persist | Task 8 (ChatRepository + ChatScreen) |
| Biography mode with timeline | Task 9 (BiographyRepository + timeline) |
| Biography generate (trigger from Profile) | Task 11 (ProfileScreen links to biography) |
| Real-time search + history + star | Task 10 (SearchRepository + SearchScreen) |
| Learning mode chat | Task 11 (LearningRepository + LearningScreen) |
| Home dashboard: news + today events + recent chat + learning | Task 6 (HomeScreen) |
| Auth guard via router redirect | Task 5 (GoRouter redirect) |
| API-first (no offline cache) | All repositories call ApiClient directly |
| ApiResponse<T> deserialization | Task 3 (api_response.dart) |

### Placeholder Scan

No "TBD", "TODO", or incomplete steps found. All code blocks are complete.

### Type Consistency

- `ChatMessage` used in chat, biography, and learning providers — same class from `lib/models/chat_message.dart`
- `ApiClient.get<T>`, `.post<T>`, `.put<T>` — consistent signatures across all repositories
- `ChatNotifier` uses `FamilyAsyncNotifier<List<ChatMessage>, int?>` — sessionId is `int?` matching `ChatScreen(sessionId: int?)`

---

**Plan complete and saved to `docs/superpowers/plans/2026-05-28-personal-ai-assistant-flutter.md`.**

**Two execution options:**

**1. Subagent-Driven (recommended)** — Fresh subagent per task, spec + quality review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
