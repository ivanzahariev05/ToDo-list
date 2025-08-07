package demo.todolist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private String password;

    @Column(name = "tasks_done")
    private int tasksDone;

    @Column
    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Task> tasks = new ArrayList<>();


    @OneToMany(mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<RefreshToken> refreshTokens = new ArrayList<>();
}

