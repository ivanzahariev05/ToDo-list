package demo.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
public class Task {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  UUID id;

    @Column
    private String title;

    @Column
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "isActive")
    private boolean isActive;

    @Column(name = "counted_as_done")
    private boolean countedAsDone = false;


    @ManyToOne
    private User owner;

}

