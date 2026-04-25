package com.zerotrust.dto;

import java.util.List;

public class RiskScoreDto {

    private int score;
    private String level;
    private List<String> factors;
    private List<String> recommendations;

    public RiskScoreDto() {}

    private RiskScoreDto(Builder b) {
        this.score           = b.score;
        this.level           = b.level;
        this.factors         = b.factors;
        this.recommendations = b.recommendations;
    }

    public int getScore()                       { return score; }
    public String getLevel()                    { return level; }
    public List<String> getFactors()            { return factors; }
    public List<String> getRecommendations()    { return recommendations; }

    public void setScore(int score)                                  { this.score = score; }
    public void setLevel(String level)                               { this.level = level; }
    public void setFactors(List<String> factors)                     { this.factors = factors; }
    public void setRecommendations(List<String> recommendations)     { this.recommendations = recommendations; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int score;
        private String level;
        private List<String> factors;
        private List<String> recommendations;

        public Builder score(int score)                              { this.score = score; return this; }
        public Builder level(String level)                           { this.level = level; return this; }
        public Builder factors(List<String> factors)                 { this.factors = factors; return this; }
        public Builder recommendations(List<String> r)               { this.recommendations = r; return this; }

        public RiskScoreDto build() { return new RiskScoreDto(this); }
    }
}
