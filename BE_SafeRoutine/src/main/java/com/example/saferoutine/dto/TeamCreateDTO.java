package com.example.saferoutine.dto;

import com.example.saferoutine.db.entity.Team;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Setter
public class TeamCreateDTO {

    private Long teamId;
    private String teamName;
    private String teamLink;
    private String teamIntro;
    private String teamImage;
    private Date teamLinkExpireDate;

    @Builder
    public TeamCreateDTO(Long teamId, String teamName, String teamLink, String teamIntro, String teamImage, Date teamLinkExpireDate) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamLink = teamLink;
        this.teamIntro = teamIntro;
        this.teamImage = teamImage;
        this.teamLinkExpireDate = teamLinkExpireDate;
    }
    private Date calculateExpirationDate() { // 실행 시간 기준 3일 만료
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expirationTime = currentTime.plusDays(3);
        return Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public Team toEntity(){
        return Team.builder()
                .teamId(teamId)
                .teamName(teamName)
                .teamLink(teamLink)
                .teamIntro(teamIntro)
                .teamImage(teamImage)
                .teamLinkExpireDate(calculateExpirationDate())
                .build();
    }
}
