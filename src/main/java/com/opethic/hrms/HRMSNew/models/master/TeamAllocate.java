package com.opethic.hrms.HRMSNew.models.master;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "team_allocation_tbl")
public class TeamAllocate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonManagedReference
    private Team team;
    @ManyToOne
    @JoinColumn(name = "team_leader_id")
    @JsonManagedReference
    private Employee teamLeader;
    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonManagedReference
    private Employee member;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    private Long createdBy;
    @Column(insertable = false)
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private Boolean status;
}
