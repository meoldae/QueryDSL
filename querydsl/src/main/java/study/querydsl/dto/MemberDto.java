package study.querydsl.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MemberDto {
    private String username;
    private int age;

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
