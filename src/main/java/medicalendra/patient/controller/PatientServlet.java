package medicalendra.patient.controller;

import medicalendra.patient.model.Patient;
import medicalendra.patient.service.PatientService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Servlet handling CRUD actions for Patient entities.
 * URL patterns:
 *   /patients            - list patients (GET)
 *   /patient/register    - patient registration page (GET)
 *   /patient/create      - create patient (POST)
 *   /patient/edit        - edit patient (POST)
 *   /patient/delete      - delete patient (POST)
 *   /patient/load        - load patient for editing (GET, id param)
 *   /patient/search      - search patients (GET, q param)
 */
@WebServlet(name = "PatientServlet",
        urlPatterns = {"/patients", "/patient/register", "/patient/create", "/patient/edit",
                "/patient/delete", "/patient/load", "/patient/search"})
public class PatientServlet extends HttpServlet {
    private PatientService patientService;

    @Override
    public void init() throws ServletException {
        // Use deterministic project-level data directory (project root + /data)
        patientService = PatientService.withServletContext(getServletContext());
    }

    // ---------------------------------------------------------------
    // GET handling: list, load for edit, search
    // ---------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/patients":
                listPatients(req, resp);
                break;
            case "/patient/register":
                req.getRequestDispatcher("/patient-register.jsp").forward(req, resp);
                break;
            case "/patient/load":
                loadPatient(req, resp);
                break;
            case "/patient/search":
                searchPatients(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void listPatients(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Patient> patients = patientService.getAllPatients();
        req.setAttribute("patients", patients);
        req.getRequestDispatcher("/patient-list.jsp").forward(req, resp);
    }

    private void loadPatient(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        Optional<Patient> opt = patientService.getPatientById(id);
        if (opt.isPresent()) {
            req.setAttribute("editPatient", opt.get());
            req.getRequestDispatcher("/patient-edit.jsp").forward(req, resp);
        } else {
            String msg = URLEncoder.encode("Patient not found", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/patients?error=" + msg);
        }
    }

    private void searchPatients(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String q = req.getParameter("q");
        List<Patient> patients = patientService.searchPatients(q);
        req.setAttribute("patients", patients);
        req.setAttribute("searchQuery", q);
        req.getRequestDispatcher("/patient-list.jsp").forward(req, resp);
    }

    // ---------------------------------------------------------------
    // POST handling: create, edit, delete
    // ---------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        switch (path) {
            case "/patient/create":
                handleCreate(req, resp);
                break;
            case "/patient/edit":
                handleEdit(req, resp);
                break;
            case "/patient/delete":
                handleDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phoneNumber");
        if (isBlank(name)) {
            resp.sendRedirect(req.getContextPath() + "/patient/register?error=" + URLEncoder.encode("Full name is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(email)) {
            resp.sendRedirect(req.getContextPath() + "/patient/register?error=" + URLEncoder.encode("Email is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(phone)) {
            resp.sendRedirect(req.getContextPath() + "/patient/register?error=" + URLEncoder.encode("Phone number is required", StandardCharsets.UTF_8));
            return;
        }
        try {
            Patient p = patientService.createPatient(name.trim(), email.trim(), phone.trim());
            String msg = URLEncoder.encode("Created ID " + p.getId(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/patient/register?msg=" + msg);
        } catch (IllegalArgumentException e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/patient/register?error=" + err);
        } catch (Exception e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/patient/register?error=" + err);
        }
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phoneNumber");
        if (isBlank(id)) {
            resp.sendRedirect(req.getContextPath() + "/patients?error=" + URLEncoder.encode("ID is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(name)) {
            resp.sendRedirect(req.getContextPath() + "/patient/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Full name is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(email)) {
            resp.sendRedirect(req.getContextPath() + "/patient/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Email is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(phone)) {
            resp.sendRedirect(req.getContextPath() + "/patient/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Phone number is required", StandardCharsets.UTF_8));
            return;
        }
        Patient p = new Patient(id.trim(), name.trim(), email.trim(), phone.trim());
        try {
            boolean ok = patientService.updatePatient(p);
            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/patients?msg=" + URLEncoder.encode("Updated", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(req.getContextPath() + "/patients?error=" + URLEncoder.encode("Update failed: Patient not found", StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/patient/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + err);
        } catch (Exception e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/patient/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + err);
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        if (isBlank(id)) {
            resp.sendRedirect(req.getContextPath() + "/patients?error=" + URLEncoder.encode("ID required", StandardCharsets.UTF_8));
            return;
        }
        boolean ok = patientService.deletePatient(id);
        if (ok) {
            resp.sendRedirect(req.getContextPath() + "/patients?msg=" + URLEncoder.encode("Deleted", StandardCharsets.UTF_8));
        } else {
            resp.sendRedirect(req.getContextPath() + "/patients?error=" + URLEncoder.encode("Delete failed", StandardCharsets.UTF_8));
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
