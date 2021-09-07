/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import de.hs_mannheim.informatik.ct.util.AttributeEncryptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(columnList = "email"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Visitor {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    @NonNull
    @Convert(converter = AttributeEncryptor.class)
    private String email;

    public Visitor(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "{email='" + email + "'}";
    }
}
