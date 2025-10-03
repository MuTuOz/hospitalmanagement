-- ============================================
-- Yararlı SELECT Sorguları
-- ============================================

-- Tüm doktorları uzmanlıkları ve departmanları ile listele
SELECT 
    d.doctor_id,
    u.name AS doctor_name,
    u.email,
    u.phone,
    s.name AS specialty,
    dep.dept_name AS department,
    d.experience,
    d.consultation_fee
FROM Doctor d
JOIN User u ON d.user_id = u.user_id
JOIN Specialty s ON d.specialty_id = s.specialty_id
LEFT JOIN Department dep ON d.dept_id = dep.dept_id
ORDER BY d.doctor_id;

-- Tüm hastaları bilgileri ile listele
SELECT 
    p.patient_id,
    u.name AS patient_name,
    u.email,
    u.phone,
    p.blood_type,
    p.date_of_birth,
    YEAR(CURDATE()) - YEAR(p.date_of_birth) AS age,
    p.insurance_no
FROM Patient p
JOIN User u ON p.user_id = u.user_id
ORDER BY p.patient_id;

-- Gelecek randevuları görüntüle
SELECT 
    a.appointment_id,
    u1.name AS doctor_name,
    u2.name AS patient_name,
    a.date_time,
    a.status,
    a.notes
FROM Appointment a
JOIN Doctor d ON a.doctor_id = d.doctor_id
JOIN User u1 ON d.user_id = u1.user_id
JOIN Patient p ON a.patient_id = p.patient_id
JOIN User u2 ON p.user_id = u2.user_id
WHERE a.date_time > NOW() AND a.status = 'scheduled'
ORDER BY a.date_time;

-- Belirli bir doktorun müsait saatlerini görüntüle
SELECT 
    u.name AS doctor_name,
    a.date,
    a.time_slot,
    a.is_booked,
    CASE 
        WHEN a.is_booked = TRUE THEN 'Reserved'
        ELSE 'Available'
    END AS status
FROM Availability a
JOIN Doctor d ON a.doctor_id = d.doctor_id
JOIN User u ON d.user_id = u.user_id
WHERE d.doctor_id = 1 
  AND a.date >= CURDATE()
ORDER BY a.date, a.time_slot;

-- Hasta tıbbi kayıtlarını görüntüle
SELECT 
    mr.record_id,
    u1.name AS patient_name,
    u2.name AS doctor_name,
    mr.record_date,
    mr.test_results,
    mr.medications,
    mr.notes
FROM Medical_Record mr
JOIN Patient p ON mr.patient_id = p.patient_id
JOIN User u1 ON p.user_id = u1.user_id
LEFT JOIN Doctor d ON mr.doctor_id = d.doctor_id
LEFT JOIN User u2 ON d.user_id = u2.user_id
WHERE p.patient_id = 2
ORDER BY mr.record_date DESC;

-- Departmanlara göre doktor sayısı
SELECT 
    dep.dept_name,
    dep.location,
    COUNT(d.doctor_id) AS doctor_count
FROM Department dep
LEFT JOIN Doctor d ON dep.dept_id = d.dept_id
GROUP BY dep.dept_id, dep.dept_name, dep.location
ORDER BY doctor_count DESC;

-- ============================================
-- Stored Procedures (Opsiyonel)
-- ============================================

DELIMITER //

-- Randevu oluşturma prosedürü
CREATE PROCEDURE CreateAppointment(
    IN p_doctor_id INT,
    IN p_patient_id INT,
    IN p_date_time DATETIME,
    IN p_notes TEXT
)
BEGIN
    DECLARE v_availability_id INT;
    
    -- Müsaitlik kontrolü
    SELECT availability_id INTO v_availability_id
    FROM Availability
    WHERE doctor_id = p_doctor_id 
      AND DATE(date) = DATE(p_date_time)
      AND time_slot = TIME(p_date_time)
      AND is_booked = FALSE
    LIMIT 1;
    
    IF v_availability_id IS NOT NULL THEN
        -- Randevu oluştur
        INSERT INTO Appointment (doctor_id, patient_id, availability_id, date_time, status, notes)
        VALUES (p_doctor_id, p_patient_id, v_availability_id, p_date_time, 'scheduled', p_notes);
        
        -- Müsaitliği güncelle
        UPDATE Availability 
        SET is_booked = TRUE 
        WHERE availability_id = v_availability_id;
        
        SELECT 'Appointment created successfully' AS message, LAST_INSERT_ID() AS appointment_id;
    ELSE
        SELECT 'Doctor is not available at this time' AS message, NULL AS appointment_id;
    END IF;
END //

-- Randevu iptal etme prosedürü
CREATE PROCEDURE CancelAppointment(
    IN p_appointment_id INT
)
BEGIN
    DECLARE v_availability_id INT;
    
    -- Müsaitlik ID'sini al
    SELECT availability_id INTO v_availability_id
    FROM Appointment
    WHERE appointment_id = p_appointment_id;
    
    -- Randevuyu iptal et
    UPDATE Appointment 
    SET status = 'cancelled' 
    WHERE appointment_id = p_appointment_id;
    
    -- Müsaitliği tekrar kullanılabilir yap
    IF v_availability_id IS NOT NULL THEN
        UPDATE Availability 
        SET is_booked = FALSE 
        WHERE availability_id = v_availability_id;
    END IF;
    
    SELECT 'Appointment cancelled successfully' AS message;
END //

DELIMITER ;

-- ============================================
-- Views (Görünümler)
-- ============================================

-- Doktor detayları görünümü
CREATE VIEW vw_doctor_details AS
SELECT 
    d.doctor_id,
    u.name AS doctor_name,
    u.email,
    u.phone,
    u.address,
    s.name AS specialty,
    dep.dept_name,
    dep.location AS dept_location,
    d.license_no,
    d.experience,
    d.consultation_fee
FROM Doctor d
JOIN User u ON d.user_id = u.user_id
JOIN Specialty s ON d.specialty_id = s.specialty_id
LEFT JOIN Department dep ON d.dept_id = dep.dept_id;

-- Randevu özeti görünümü
CREATE VIEW vw_appointment_summary AS
SELECT 
    a.appointment_id,
    a.date_time,
    a.status,
    u1.name AS doctor_name,
    s.name AS specialty,
    u2.name AS patient_name,
    p.blood_type,
    a.notes,
    a.diagnosis
FROM Appointment a
JOIN Doctor d ON a.doctor_id = d.doctor_id
JOIN User u1 ON d.user_id = u1.user_id
JOIN Specialty s ON d.specialty_id = s.specialty_id
JOIN Patient p ON a.patient_id = p.patient_id
JOIN User u2 ON p.user_id = u2.user_id;

-- ============================================
-- Triggers (Tetikleyiciler)
-- ============================================

DELIMITER //

-- Randevu oluşturulduğunda müsaitliği otomatik güncelle
CREATE TRIGGER trg_update_availability_after_appointment
AFTER INSERT ON Appointment
FOR EACH ROW
BEGIN
    IF NEW.availability_id IS NOT NULL THEN
        UPDATE Availability 
        SET is_booked = TRUE 
        WHERE availability_id = NEW.availability_id;
    END IF;
END //

-- Randevu iptal edildiğinde müsaitliği geri getir
CREATE TRIGGER trg_restore_availability_after_cancel
AFTER UPDATE ON Appointment
FOR EACH ROW
BEGIN
    IF NEW.status = 'cancelled' AND OLD.status != 'cancelled' THEN
        IF NEW.availability_id IS NOT NULL THEN
            UPDATE Availability 
            SET is_booked = FALSE 
            WHERE availability_id = NEW.availability_id;
        END IF;
    END IF;
END //

DELIMITER ;