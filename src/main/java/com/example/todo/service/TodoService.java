package com.example.todo.service;

import com.example.todo.audit.Auditable;
import com.example.todo.dto.MonthlyProgressSummary;
import com.example.todo.entity.AppUser;
import com.example.todo.entity.Category;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Status;
import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoHistory;
import com.example.todo.exception.BusinessException;
import com.example.todo.form.TodoForm;
import com.example.todo.mapper.TodoMapper;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.repository.TodoHistoryRepository;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ToDoの業務ロジックを提供するサービスです。
 * <p>
 * 登録・更新・削除・検索・CSV出力用取得などのユースケースをまとめて扱います。
 *
 * @author Task-List Team
 * @version 1.0
 * @since 1.0
 * @see com.example.todo.controller.TodoController
 */
@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TodoHistoryRepository todoHistoryRepository;
    private final AsyncTaskService asyncTaskService;

    /**
     * コンストラクタです。
     *
     * @param todoRepository ToDo永続化リポジトリ
     * @param todoMapper MyBatisマッパー
     * @param categoryRepository カテゴリリポジトリ
     * @param userRepository ユーザーリポジトリ
     * @param todoHistoryRepository 履歴リポジトリ
     * @param asyncTaskService 非同期処理サービス
     */
    public TodoService(TodoRepository todoRepository, TodoMapper todoMapper,
                       CategoryRepository categoryRepository, UserRepository userRepository,
                       TodoHistoryRepository todoHistoryRepository,
                       AsyncTaskService asyncTaskService) {
        this.todoRepository = todoRepository;
        this.todoMapper = todoMapper;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.todoHistoryRepository = todoHistoryRepository;
        this.asyncTaskService = asyncTaskService;
    }

    /**
     * ToDoを新規保存します。
     *
     * @param form 入力フォーム
     * @param userId 保存対象ユーザーID
     * @return 保存後のToDo
     * @throws BusinessException 業務例外が発生した場合
     */
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
    @Auditable(action = "CREATE", entityType = "Todo", entityIdExpression = "#result.id")
    public Todo save(TodoForm form, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Todo todo = new Todo();
        todo.setAuthor(form.getAuthor());
        todo.setAssignee(form.getAssignee());
        todo.setTitle(form.getTitle());
        todo.setDetail(form.getDetail());
        todo.setPriority(form.getPriority() == null ? Priority.MEDIUM : form.getPriority());
        todo.setStatus(form.getStatus() == null ? Status.NOT_STARTED : form.getStatus());
        todo.setCategory(resolveCategory(form.getCategoryId()));
        todo.setStartDate(form.getStartDate());
        todo.setDeadline(form.getDeadline());
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        todo.setUser(resolveUser(userId));
        Todo saved = todoRepository.save(todo);

        TodoHistory history = new TodoHistory();
        history.setTodoId(saved.getId());
        history.setAction("CREATE");
        history.setDetail(saved.getTitle());
        todoHistoryRepository.save(history);

        asyncTaskService.sendEmail(saved.getId());
        return saved;
    }

    /**
     * 全ToDoを作成日降順で取得します。
     *
     * @return ToDo一覧
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 指定ユーザーのToDoを取得します。
     *
     * @param userId ユーザーID
     * @return ToDo一覧
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public List<Todo> findAllByUserId(Long userId) {
        return todoRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
    }

    /**
     * タイトルキーワードで検索します。
     *
     * @param keyword 検索キーワード
     * @return 検索結果
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public List<Todo> searchByTitle(String keyword) {
        return todoRepository.searchByTitle(keyword);
    }

    /**
     * 一覧表示用に条件付きで取得します。
     *
     * @param keyword キーワード
     * @param categoryId カテゴリID
     * @param userId ユーザーID
     * @param includeCompleted 完了を含めるか
     * @param sort ソートキー
     * @param dir ソート方向
     * @param limit 取得件数
     * @param offset オフセット
     * @return 一覧
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public List<Todo> findAllSorted(String keyword, Long categoryId, Long userId, boolean includeCompleted,
                                    String sort, String dir, int limit, int offset) {
        return todoMapper.findAllSorted(keyword, categoryId, userId, includeCompleted, sort, dir, limit, offset);
    }

    /**
     * 条件に合致する件数を返します。
     *
     * @param keyword キーワード
     * @param categoryId カテゴリID
     * @param userId ユーザーID
     * @param includeCompleted 完了を含めるか
     * @return 件数
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public long countAll(String keyword, Long categoryId, Long userId, boolean includeCompleted) {
        return todoMapper.countAll(keyword, categoryId, userId, includeCompleted);
    }

    /**
     * IDでToDoを取得します。
     *
     * @param id ToDo ID
     * @param userId ユーザーID（{@code null} の場合は全体検索）
     * @return ToDo。見つからない場合は {@code null}
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public Todo findById(Long id, Long userId) {
        if (userId == null) {
            return todoRepository.findById(id).orElse(null);
        }
        return todoRepository.findByIdAndUser_Id(id, userId).orElse(null);
    }

    /**
     * ToDoを更新します。
     *
     * @param form 入力フォーム
     * @param userId ユーザーID
     * @return 更新後ToDo。対象なしの場合は {@code null}
     * @throws RuntimeException 更新処理中に予期しない例外が発生した場合
     */
    @Auditable(action = "UPDATE", entityType = "Todo", entityIdExpression = "#form.id")
    public Todo update(TodoForm form, Long userId) {
        Todo todo = findById(form.getId(), userId);
        if (todo == null) {
            return null;
        }
        todo.setAuthor(form.getAuthor());
        todo.setAssignee(form.getAssignee());
        todo.setTitle(form.getTitle());
        todo.setDetail(form.getDetail());
        todo.setPriority(form.getPriority() == null ? Priority.MEDIUM : form.getPriority());
        todo.setStatus(form.getStatus() == null
                ? (todo.getStatus() == null ? Status.NOT_STARTED : todo.getStatus())
                : form.getStatus());
        todo.setCategory(resolveCategory(form.getCategoryId()));
        todo.setStartDate(form.getStartDate());
        todo.setDeadline(form.getDeadline());
        todo.setUpdatedAt(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    /**
     * ToDoを削除します。
     *
     * @param id ToDo ID
     * @param userId ユーザーID（{@code null} の場合は管理者削除）
     * @throws RuntimeException 削除処理中に予期しない例外が発生した場合
     */
    @Transactional
    @Auditable(action = "DELETE", entityType = "Todo", entityIdExpression = "#id")
    public void delete(Long id, Long userId) {
        if (userId == null) {
            todoRepository.deleteById(id);
            return;
        }
        todoRepository.deleteByIdAndUser_Id(id, userId);
    }

    /**
     * ステータスを次段階へ進めます。
     *
     * @param id ToDo ID
     * @param userId ユーザーID
     * @return 更新できた場合は {@code true}
     * @throws RuntimeException 更新処理中に予期しない例外が発生した場合
     */
    @Auditable(action = "ADVANCE_STATUS", entityType = "Todo", entityIdExpression = "#id")
    public boolean advanceStatus(Long id, Long userId) {
        Todo todo = findById(id, userId);
        if (todo == null) {
            return false;
        }
        Status current = todo.getStatus() == null ? Status.NOT_STARTED : todo.getStatus();
        todo.setStatus(current.next());
        todo.setUpdatedAt(LocalDateTime.now());
        todoRepository.save(todo);
        return true;
    }

    /**
     * ステータスを明示的に設定します。
     *
     * @param id ToDo ID
     * @param status 設定するステータス
     * @param userId ユーザーID
     * @return 更新できた場合は {@code true}
     * @throws RuntimeException 更新処理中に予期しない例外が発生した場合
     */
    @Auditable(action = "SET_STATUS", entityType = "Todo", entityIdExpression = "#id")
    public boolean updateStatus(Long id, Status status, Long userId) {
        Todo todo = findById(id, userId);
        if (todo == null) {
            return false;
        }
        todo.setStatus(status == null ? Status.NOT_STARTED : status);
        todo.setUpdatedAt(LocalDateTime.now());
        todoRepository.save(todo);
        return true;
    }

    /**
     * 複数ToDoを一括削除します。
     *
     * @param ids 削除対象ID一覧
     * @param userId ユーザーID
     * @return 削除件数
     * @throws RuntimeException 削除処理中に予期しない例外が発生した場合
     */
    @Transactional
    @Auditable(action = "BULK_DELETE", entityType = "Todo")
    public int deleteByIds(List<Long> ids, Long userId) {
        return todoMapper.deleteByIds(ids, userId);
    }

    /**
     * CSV出力用にToDo一覧を取得します。
     *
     * @param keyword キーワード
     * @param categoryId カテゴリID
     * @param userId ユーザーID
     * @param sort ソートキー
     * @param dir ソート方向
     * @return CSV出力対象一覧
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public List<Todo> findAllForCsv(String keyword, Long categoryId, Long userId, String sort, String dir) {
        return todoMapper.findAllForCsv(keyword, categoryId, userId, sort, dir);
    }

    /**
     * 月次進捗サマリを取得します。
     *
     * @param userId ユーザーID
     * @param start 集計開始日
     * @param end 集計終了日
     * @return 月次進捗サマリ
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public MonthlyProgressSummary findMonthlyProgressSummary(Long userId, LocalDate start, LocalDate end) {
        return todoMapper.findMonthlyProgressSummary(userId, start, end);
    }

    /**
     * 指定ToDoの所有者か判定します。
     *
     * @param todoId ToDo ID
     * @param username ユーザー名
     * @return 所有者であれば {@code true}
     * @throws RuntimeException 判定処理中に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long todoId, String username) {
        if (todoId == null || username == null || username.isBlank()) {
            return false;
        }
        return todoRepository.existsByIdAndUser_Username(todoId, username);
    }

    /**
     * ユーザー名からユーザーを取得します。
     *
     * @param username ユーザー名
     * @return ユーザー。見つからない場合は {@code null}
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    @Transactional(readOnly = true)
    public AppUser findUserByUsername(String username) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    /**
     * カテゴリIDからカテゴリを解決します。
     *
     * @param categoryId カテゴリID
     * @return カテゴリ。未指定・未存在の場合は {@code null}
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).orElse(null);
    }

    /**
     * ユーザーIDからユーザーを解決します。
     *
     * @param userId ユーザーID
     * @return ユーザー。未指定・未存在の場合は {@code null}
     * @throws RuntimeException データ取得時に予期しない例外が発生した場合
     */
    private AppUser resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }
}

