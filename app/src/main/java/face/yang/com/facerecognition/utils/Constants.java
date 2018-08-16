package face.yang.com.facerecognition.utils;

import java.util.List;

import face.yang.com.facerecognition.entity.UserEntity;

public class Constants {

    //分组id
    public final static String groupId="1";
    //获取所有成员的key
    public final static String groupKey="groupKey";
    public static List<UserEntity> userList;

    public enum UsetType{
        black("黑明单","black"),
        white("白名单","white"),
        VIP("VIP","VIP"),
        employee("员工","employee"),
        other("其他","other");

        private String name;
        private String value;
        private UsetType(String name,String value){
            this.name=name;
            this.value=value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
