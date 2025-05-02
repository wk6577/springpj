package com.milestone.service;

import com.milestone.dto.MemberSearchDto;
import com.milestone.entity.Member;
import com.milestone.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;




    @Transactional
    public List<Member> memberIdSearch (String query){


        List<Member> members = null;

        try{
            members = memberRepository.findByMemberNicknameContainingAndMemberVisibleAndMemberStatusOrderByMemberLastloginDesc(query, "public", "active").orElseThrow(() -> new IllegalArgumentException("검색결과가 없습니다."));

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("아이디 검색 중 오류가 발생하였습니다");
        }

        return members;
    }

}
