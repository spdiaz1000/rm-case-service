package uk.gov.ons.ctp.response.iac.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Domain model object for representation of the update request body object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UpdateInternetAccessCodeDTO {

  @NotNull
  private String updatedBy;


}
