package Classes;

import AppUsers.Doctor;
import AppUsers.Patient;
import AppUsers.Pharmacy;

/**
 * <h2>Created by Marius Baltramaitis on 09/09/2017.</h2>
 *
 * <p>Prescription class</p>
 */
public class Prescription {

    private String ID,specialNotes;
    private Medicine medicine;
    private Doctor doctor;
    private Patient patient;
    private Pharmacy pharmacy;
    private int available,Medicine_ID,Doctor_ID,Patient_ID,Pharmacy_ID;


    /**
     * Default constructor
     */
    public Prescription() {}

    /**
     *
     * @param ID ID
     * @param medicine_ID Medicine ID
     * @param doctor_ID Doctor ID
     * @param patient_ID Patient ID
     * @param pharmacy_ID Pharmacy ID
     * @param specialNotes special notes
     * @param available 1 if available 0 if disabled
     */
    public Prescription(String ID, int medicine_ID, int doctor_ID, int patient_ID, int pharmacy_ID, String specialNotes,int available) {
        this.ID = ID;
        Medicine_ID = medicine_ID;
        Doctor_ID = doctor_ID;
        Patient_ID = patient_ID;
        Pharmacy_ID = pharmacy_ID;
        this.specialNotes = specialNotes;
        this.available = available;
    }

    /**
     *
     * @param ID Prescription ID
     * @param specialNotes special notes
     * @param medicine medicine object
     * @param doctor doctor object
     * @param patient patient object
     * @param pharmacy pharmacy object
     * @param available 1 if available 0 if disabled
     */
    public Prescription(String ID, String specialNotes, Medicine medicine, Doctor doctor, Patient patient, Pharmacy pharmacy, int available) {
        this.ID = ID;
        this.specialNotes = specialNotes;
        this.medicine = medicine;
        this.doctor = doctor;
        this.patient = patient;
        this.pharmacy = pharmacy;
        this.available = available;
    }

    /**
     * getter for medicine id
     * @return medicine id
     */
    public int getMedicine_ID() {
        if(medicine != null)
            return Integer.parseInt(medicine.getID());

        return Medicine_ID;
    }

    /**
     * Getter for doctor id
     * @return doctor id
     */
    public int getDoctor_ID() {
        if(doctor != null)
            return Integer.parseInt(doctor.getID());

        return Doctor_ID;
    }

    /**
     * getter for patient id
     * @return patient id
     */
    public int getPatient_ID() {
        if(patient != null)
            return Integer.parseInt(patient.getID());
        return Patient_ID;
    }

    /**
     * getter for pharmacy id
     * @return pharmacy id
     */
    public int getPharmacy_ID() {
        if(pharmacy != null)
            return Integer.parseInt(pharmacy.getID());

        return Pharmacy_ID;
    }

    /**
     * Getter of prescription id
     * @return prescription id
     */
    public String getID() {return ID;}

    /**
     * Setter of prescription ID
     * @param ID prescription ID
     */
    public void setID(String ID) {this.ID = ID;}

    /**
     * Getter of special notes
     * @return special notes
     */
    public String getSpecialNotes() {return specialNotes;}

    /**
     * Setter of special notes
     * @param specialNotes special notes
     */
    public void setSpecialNotes(String specialNotes) {this.specialNotes = specialNotes;}

    /**
     * Getter of medicine object
     * @return medicine object
     */
    public Medicine getMedicine() {return medicine;}

    /**
     * Setter for medicine object
     * @param medicine pharmacy object
     */
    public void setMedicine(Medicine medicine) {this.medicine = medicine;}

    /**
     * Getter of doctor object
     * @return doctor object
     */
    public Doctor getDoctor() {return doctor;}

    /**
     * Setter for doctor object
     * @param doctor pharmacy object
     */
    public void setDoctor(Doctor doctor) {this.doctor = doctor;}

    /**
     * Getter of patient object
     * @return patient object
     */
    public Patient getPatient() {return patient;}

    /**
     * Setter for patient object
     * @param patient pharmacy object
     */
    public void setPatient(Patient patient) {this.patient = patient;}

    /**
     * Getter of pharmacy object
     * @return pharmacy object
     */
    public Pharmacy getPharmacy() {return pharmacy;}

    /**
     * Setter for pharmacy object
     * @param pharmacy pharmacy object
     */
    public void setPharmacy(Pharmacy pharmacy) {this.pharmacy = pharmacy;}

    /**
     * Getter for available
     * @return 1 if available, 0 if sidabled
     */
    public int getAvailable() {return available;}

    /**
     * Setter for available
     * @param available 1 if available 0 if disabled
     */
    public void setAvailable(int available) {this.available = available;}

    /**
     *
     * @return medicine if not null, "Properly undefined" otherwise
     */
    @Override
    public String toString()
    {
        return (medicine == null) ? "Properly undefined" : medicine.toString();

    }
}
