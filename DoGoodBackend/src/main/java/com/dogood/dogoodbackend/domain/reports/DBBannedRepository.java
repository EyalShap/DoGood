package com.dogood.dogoodbackend.domain.reports;

import com.dogood.dogoodbackend.jparepos.BannedJPA;
import com.dogood.dogoodbackend.jparepos.ReportJPA;
import com.dogood.dogoodbackend.utils.ReportErrors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DBBannedRepository implements BannedRepository{
    private BannedJPA jpa;

    public DBBannedRepository(BannedJPA jpa) {
        this.jpa = jpa;
    }

    @Override
    public void ban(String email) {
        if(email.startsWith("\"") && email.endsWith("\"")) {
            int len = email.length();
            email = email.substring(1, len - 1);
        }
        Banned banned = new Banned(email);
        jpa.save(banned);
    }

    @Override
    public void unban(String email) {
        if(email.startsWith("\"") && email.endsWith("\"")) {
            int len = email.length();
            email = email.substring(1, len - 1);
        }
        jpa.deleteById(email);
    }

    @Override
    public boolean isBanned(String email) {
        if(email.startsWith("\"") && email.endsWith("\"")) {
            int len = email.length();
            email = email.substring(1, len - 1);
        }
        return jpa.existsById(email);
    }

    @Override
    public List<String> getBannedEmails() {
        List<Banned> allBanned = jpa.findAll();
        List<String> allBannedEmails = allBanned.stream().map(banned -> banned.getEmail()).collect(Collectors.toList());
        return allBannedEmails;
    }
}
