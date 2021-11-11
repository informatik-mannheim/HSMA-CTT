package de.hs_mannheim.informatik.ct.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor @Getter
public class MailAddress {
    private String mailAddress;
    private String name;
    private String domain;
    private String tld;

    public static MailAddress parse(String mailAddress) throws MailAddressInvalidException {
        var chunks = split(mailAddress, "@");
        if(chunks.size()!=2) throw new MailAddressInvalidException();
        val name = chunks.get(0);
        chunks = split(chunks.get(1), "\\.");
        if(chunks.size()<2) throw new MailAddressInvalidException();
        val tld = chunks.get(chunks.size() - 1);
        val domain = joinWithoutLast(chunks, ".");
        return new MailAddress(mailAddress, name, domain, tld);
    }

    private static List<String> split(String data, String delimiter){
        return Arrays.asList(data.split(delimiter));
    }

    private static String joinWithoutLast(List<String> data, String glue){
        String domain = data.get(0);
        for(int i=1;i < data.size() - 1;i++){
            domain += glue + data.get(i);
        }
        return domain;
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Mail Address invalid")
    public static class MailAddressInvalidException extends RuntimeException {
    }
}
