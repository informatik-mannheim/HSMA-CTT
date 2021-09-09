package de.hs_mannheim.informatik.ct.model;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity

@NoArgsConstructor
@Getter
@Setter
public class ExternalVisitor extends Visitor {
    @Column
    @NonNull
    private String name;
    @Column
    private String number;
    @Column
    private String address;

    public static ExternalVisitor visitorWithPhone(String email, String name, String number) {
        return new ExternalVisitor(email, name, number, null);
    }

    public static ExternalVisitor visitorWithAddress(String email, String name, String address) {
        return new ExternalVisitor(email, name, null, address);
    }

    public ExternalVisitor(String email, String name, String number, String address) {
        super(email);
        this.name = name;
        this.number = number;
        this.address = address;
    }

    @Override
    public String getName() {
        return this.getName();
    }

    @Override
    public String getAddress () {
        return this.address;
    }

}
