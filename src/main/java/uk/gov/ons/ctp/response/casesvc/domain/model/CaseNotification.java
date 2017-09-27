package uk.gov.ons.ctp.response.casesvc.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * Domain model object.
 */
@CoverageIgnore
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "casenotification", schema = "casesvc")
public class CaseNotification implements Serializable {

  private static final long serialVersionUID = -2823008455120203044L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "casenotificationidseq_gen")
  @GenericGenerator(name = "casenotificationidseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @org.hibernate.annotations.Parameter(name = "sequence_name", value = "casesvc.casenotificationseq"),
          @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
      })
  @Column(name = "casenotificationPK")
  private int caseNotificationPK;

  @Column(name = "caseid")
  private UUID caseId;

  @Column(name = "actionplanid")
  private UUID actionPlanId;

  @Column(name = "notificationtype")
  private String notificationType;
}
