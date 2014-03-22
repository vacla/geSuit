package net.cubespace.geSuit.Database.Table;

import com.j256.ormlite.field.DatabaseField;
import lombok.Data;

import javax.persistence.Entity;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
@Data
@Entity
public class Player {
    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(unique = true)
    private String name;
    @DatabaseField(canBeNull = true)
    private String uuid;
}
