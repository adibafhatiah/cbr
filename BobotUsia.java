package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.db.jpa.Model;
import play.db.jpa.Transactional;
@Entity
@Transactional
@Table(name="bobotusia", uniqueConstraints={@UniqueConstraint(columnNames={"id"})})
public class BobotUsia extends Model {
	
	public Double bobotusia;
	public int kelas;
	

}
