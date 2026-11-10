package kr.hs.dgsw.ticketzone.domain.performance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void delete() {
        this.deleted = true;
    }
}
