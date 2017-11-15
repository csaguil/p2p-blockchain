import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class Blockchain {

    private Block head;
    private ArrayList<Transaction> pool;
    private int length;

    public Blockchain() {
        pool = new ArrayList<>();
        length = 0;
    }

    public synchronized Block getHead() {
        return head;
    }

    public synchronized void setHead(Block head) {
        this.head = head;
    }

    public synchronized int getLength() {
        return length;
    }

    public synchronized void setLength(int length) {
        this.length = length;
    }

    public synchronized ArrayList<Transaction> getPool() {
        return pool;
    }

    public synchronized void setPool(ArrayList<Transaction> pool) {
        this.pool = pool;
    }

    public synchronized boolean addTransaction(String txString) {
        String[] tokens = txString.split("\\|");
        if (tokens.length != 3) {
            return false;
        }
        if (!tokens[0].equals("tx")) {
            return false;
        }
        Transaction transaction = new Transaction();
        transaction.setSender(tokens[1]);
        transaction.setContent(tokens[2]);
        if (!transaction.isValid()) {
            return false;
        }
        pool.add(transaction);
        return true;
    }

    public synchronized boolean commit(int nonce) {
        if (pool.size() == 0) {
            return false;
        }

        Block newBlock = new Block();
        if (head == null) {
            newBlock.setPreviousHash(new byte[32]);
        } else {
            newBlock.setPreviousHash(head.calculateHash());
        }
        newBlock.setTransactions(pool);
        byte[] hash = newBlock.calculateHashWithNonce(nonce);
        String hashString = Base64.getEncoder().encodeToString(hash);
        if(hashString.startsWith("A")) {
            newBlock.setPreviousBlock(head);
            head = newBlock;
            pool = new ArrayList<>();
            length += 1;
            return true;
        }
        return false;
    }

    public synchronized String toString() {
        String cutOffRule = new String(new char[81]).replace("\0", "-") + "\n";
        String poolString = "";
        for (Transaction tx : pool) {
            poolString += tx.toString();
        }

        String blockString = "";
        Block bl = head;
        while (bl != null) {
            blockString += bl.toString();
            bl = bl.getPreviousBlock();
        }

        return "Pool:\n"
                + cutOffRule
                + poolString
                + cutOffRule
                + blockString;
    }
}
