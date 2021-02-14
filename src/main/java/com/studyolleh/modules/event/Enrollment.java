package com.studyolleh.modules.event;

import com.studyolleh.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(
        name = "Enrollment.withEventAndStudyGraph",
        attributeNodes = {
                @NamedAttributeNode(value = "event", subgraph = "withStudy")
        },
        subgraphs = @NamedSubgraph(name = "withStudy", attributeNodes = @NamedAttributeNode("study"))
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;
}
