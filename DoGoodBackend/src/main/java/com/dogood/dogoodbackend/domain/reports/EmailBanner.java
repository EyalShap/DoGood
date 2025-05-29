package com.dogood.dogoodbackend.domain.reports;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailBanner {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void banEmail(BannedRepository bannedRepository, String emailToBan){
        if(bannedRepository.isBanned(emailToBan)){
            throw new IllegalArgumentException("Email already banned");
        }
        bannedRepository.ban(emailToBan);
    }
}
