package com.dogood.dogoodbackend.domain.reports;

import java.util.List;

public interface BannedRepository {
    public void ban(String email);
    public void unban(String email);
    public boolean isBanned(String username);
    public List<String> getBannedEmails();
}
