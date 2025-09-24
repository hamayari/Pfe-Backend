package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeamCollaborationDTO {
    private List<TeamMember> teamMembers;
    private List<InternalComment> recentComments;
    private List<EscalationItem> activeEscalations;
    private int totalTeamMembers;
    private int activeMembers;
    private int pendingTasks;
    private int completedTasks;
}
