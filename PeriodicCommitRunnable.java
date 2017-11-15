import java.security.SecureRandom;

public class PeriodicCommitRunnable implements Runnable{

    private volatile boolean isRunning;
    private int nonce;
    private Blockchain blockchain;
    private SecureRandom randomGenerator;

    public PeriodicCommitRunnable(Blockchain blockchain) {
        isRunning = true;
        this.blockchain = blockchain;
        randomGenerator = new SecureRandom();
        nonce = randomGenerator.nextInt();
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean getRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
            blockchain.commit(nonce);
            nonce = randomGenerator.nextInt();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
            }
        }
    }
}
