package game;

public class ScoreRecord {
    private String name;
    private int stage1;
    private int stage2;
    private int stage3;
    private int totalScore;

    public ScoreRecord() {}

    public ScoreRecord(String name, int s1, int s2, int s3, int total) {
        this.name = name;
        this.stage1 = s1;
        this.stage2 = s2;
        this.stage3 = s3;
        this.totalScore = total;
    }
    public String getName() { return name; }
    public int getStage1() { return stage1; }
    public int getStage2() { return stage2; }
    public int getStage3() { return stage3; }
    public int getTotalScore() { return totalScore; }
}