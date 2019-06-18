package umairayub.tapcounter;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Count {
    @Id
    private long id;

    private int count;
    private String countName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCountName() {
        return countName;
    }

    public void setCountName(String countName) {
        this.countName = countName;
    }
}
