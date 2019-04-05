package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.db.jpa.Model;
@Entity
public class DetailDiagnosis extends Model {
	@ManyToOne
	public Gejala gejala;
	@ManyToOne
	public Diagnosis diagnosis;
	@ManyToOne
	public DerajatGejala derajat;

}
