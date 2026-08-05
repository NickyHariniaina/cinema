package hei.student.school.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Genre {
  THRILLER,
  ROMANCE,
  COMEDY,
  DRAMA,
  ACTION,
  @JsonProperty("SCI-FI")
  SCI_FI,
  FANTASY,
  ANIMATION
}
