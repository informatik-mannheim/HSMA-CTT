package de.hs_mannheim.informatik.ct.util;

import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor @Getter
@Slf4j
public class MailAddress {
    private String mailAddress;
    private String name;
    private String domain;
    private String tld;

    private static final String EMAIL_REGEX = "(.*)@(.*)\\.(.*)$";
    private static final String DEFAULT_INVALID_MAIL = "invalid-email@invalid.org";

    public static MailAddress parse(String mailAddress) {
        val match = splitMailAddress(mailAddress);
        val name = match.group(1);
        val domain = match.group(2);
        val tld = match.group(3);
        return new MailAddress(mailAddress, name, domain, tld);
    }

    private static Matcher splitMailAddress(String mailAddress){
        Matcher match = Pattern.compile(EMAIL_REGEX).matcher(mailAddress);
        if(!match.matches()) {
            match = Pattern.compile(EMAIL_REGEX).matcher(DEFAULT_INVALID_MAIL);
            log.error("support mail address '"+mailAddress+"' is invalid");
        }
        return match;
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
}
