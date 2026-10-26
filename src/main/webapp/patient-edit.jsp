<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Update Patient - MediCalendra</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
    <script src="${pageContext.request.contextPath}/js/patients.js" defer></script>
</head>
<body>
    <aside class="sidebar">
        <div class="sidebar-brand">
            <div class="sidebar-brand-icon">
                <img src="${pageContext.request.contextPath}/assets/icons/logo-heart-pulse.svg" alt="MediCalendra Logo" class="brand-icon" width="40" height="40" />
            </div>
            <div class="sidebar-brand-text">
                <h2>MediCalendra</h2>
                <span>Healthcare Portal</span>
            </div>
            <img src="${pageContext.request.contextPath}/assets/icons/menu.svg" alt="Toggle Menu" class="menu-icon" width="22" height="22" />
        </div>
        <nav>
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/">
                        <img src="${pageContext.request.contextPath}/assets/icons/dashboard.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Dashboard</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/patient/register">
                        <img src="${pageContext.request.contextPath}/assets/icons/user-plus.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Patient Registration</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/patients" class="active">
                        <img src="${pageContext.request.contextPath}/assets/icons/users.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Patient List</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/appointment/schedule">
                        <img src="${pageContext.request.contextPath}/assets/icons/calendar-plus.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Schedule Appointment</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/appointments">
                        <img src="${pageContext.request.contextPath}/assets/icons/calendar-days.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Appointment List</span>
                    </a>
                </li>
            </ul>
        </nav>
    </aside>
    <div class="main-content">
        <div class="page-header">
            <div>
                <h1>Update Patient</h1>
                <p style="margin:4px 0 0 0; color:#64748b; font-size:0.95rem;">Modify existing patient details for record <strong><c:out value="${editPatient.id}"/></strong>.</p>
            </div>
            <a href="${pageContext.request.contextPath}/patients" class="btn secondary">
                <img src="${pageContext.request.contextPath}/assets/icons/users.svg" alt="" class="action-icon" width="19" height="19" />
                <span>Back to Patient List</span>
            </a>
        </div>
        <c:if test="${not empty param.msg}">
            <div class="msg success">
                <img src="${pageContext.request.contextPath}/assets/icons/check-circle.svg" alt="" class="action-icon" width="19" height="19" />
                <span><c:out value="${param.msg}"/></span>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="msg error"><c:out value="${param.error}"/></div>
        </c:if>
        <section class="form-section">
            <h2>Edit Patient Details</h2>
            <form method="post" action="${pageContext.request.contextPath}/patient/edit">
                <input type="hidden" name="id" value="${editPatient.id}" />
                <div class="form-grid">
                    <div class="form-group">
                        <label for="fullName">Full Name</label>
                        <input type="text" id="fullName" name="fullName" value="${editPatient.fullName}" placeholder="e.g. John Silva" required />
                    </div>
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" value="${editPatient.email}" placeholder="e.g. john.silva@example.com" required />
                    </div>
                    <div class="form-group">
                        <label for="phoneNumber">Phone Number</label>
                        <input type="text" id="phoneNumber" name="phoneNumber" value="${editPatient.phoneNumber}" placeholder="e.g. 0771234567 or +94771234567" required />
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn primary">
                        <img src="${pageContext.request.contextPath}/assets/icons/edit.svg" alt="" class="action-icon" width="19" height="19" />
                        <span>Update Patient</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/patients" class="btn secondary">Cancel</a>
                </div>
            </form>
        </section>
    </div>
</body>
</html>

