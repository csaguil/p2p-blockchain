import java.io.Serializable;

public class Transaction implements Serializable{

    private String sender;
    private String content;
    private boolean isValid;

    public Transaction() {
        isValid = false;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
        isValid = validate();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        isValid = validate();
    }

    public String toString() {
        if (isValid) {
            return String.format("|%s|%70s|\n", sender, content);
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (!isValid) {
            return false;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        Transaction tx = (Transaction) other;
        if (content.equals(tx.getContent()) && sender.equals(tx.getSender())) {
            return true;
        }
        return false;
    }

    private boolean validate() {
        if (sender == null || content == null) {
            return false;
        }
        if (!sender.matches("^[a-z]{4}[0-9]{4}$")) {
            return false;
        }
        if (content.contains("\\|") || content.length() > 70) {
            return false;
        }
        return true;
    }

    public boolean isValid() {
        return isValid;
    }

}
