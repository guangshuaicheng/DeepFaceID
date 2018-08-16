package face.yang.com.facerecognition.entity;

import java.io.Serializable;
import java.util.List;

public class GroupEntity implements Serializable {
    public List<String> username;

    public List<String> getUsername() {
        return username;
    }

    public void setUsername(List<String> username) {
        this.username = username;
    }
}
