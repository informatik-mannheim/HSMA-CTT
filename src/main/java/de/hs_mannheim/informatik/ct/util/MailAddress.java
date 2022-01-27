package de.hs_mannheim.informatik.ct.util;

import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@AllArgsConstructor @Getter
public class MailAddress {
    private String mailAddress;
    private String name;
    private String domain;
    private String tld;

    private static final Pattern email_splitter_regex = Pattern.compile("(.*)@(.*)\\.(.*)$");

    public static MailAddress parse(String mailAddress) throws InvalidEmailException {
        val match = email_splitter_regex.matcher(mailAddress);
        if(!match.matches()) {
            throw new InvalidEmailException();
        }

        val name = match.group(1);
        val domain = match.group(2);
        val tld = match.group(3);

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
