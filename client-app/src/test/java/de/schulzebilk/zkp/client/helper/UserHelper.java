package de.schulzebilk.zkp.client.helper;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.model.User;

public class UserHelper {

    public static User getFiatShamirUser() {
        return new User("alice", "password123", AuthType.FIATSHAMIR);
    }

    public static User getPasswordUser() {
        return new User("charlie", "secretpass333", AuthType.PASSWORD);
    }

    public static User getSignatureUser() {
        return new User("dave", "mysecretpassword", AuthType.SIGNATURE);
    }
}
