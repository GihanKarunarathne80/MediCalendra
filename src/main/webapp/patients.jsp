<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Patient Management - MediCalendra</title>
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
                    <a href="${pageContext.request.contextPath}/patients" class="active">
                        <img src="${pageContext.request.contextPath}/assets/icons/users.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Patients</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/appointments">
                        <img src="${pageContext.request.contextPath}/assets/icons/calendar-days.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Appointments</span>
                    </a>
                </li>
            </ul>
        </nav>
    </aside>
    <div class="main-content">
        <h1>Patient Management</h1>
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
            <h2><c:choose><c:when test="${not empty editPatient}">Edit Patient</c:when><c:otherwise>Register New Patient</c:otherwise></c:choose></h2>
            <form method="post" action="${pageContext.request.contextPath}${not empty editPatient ? '/patient/edit' : '/patient/create'}">
                <c:if test="${not empty editPatient}">
                    <input type="hidden" name="id" value="${editPatient.id}" />
                </c:if>
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
                    <c:choose>
                        <c:when test="${not empty editPatient}">
                            <img src="${pageContext.request.contextPath}/assets/icons/edit.svg" alt="" class="action-icon" width="19" height="19" />
                            <span>Update Patient</span>
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/icons/user-plus.svg" alt="" class="action-icon" width="19" height="19" />
                            <span>Create Patient</span>
                        </c:otherwise>
                    </c:choose>
                </button>
                <c:if test="${not empty editPatient}">
                <a href="${pageContext.request.contextPath}/patients" class="btn secondary">Cancel</a>
                </c:if>
                </div>
            </form>
        </section>
        <section class="table-section">
            <h2>Patients List</h2>
            <form method="get" action="${pageContext.request.contextPath}/patient/search" class="search-form">
                <input type="text" name="q" placeholder="Search patients..." value="${searchQuery}" />
                <button type="submit" class="btn">
                    <img src="${pageContext.request.contextPath}/assets/icons/search.svg" alt="" class="action-icon" width="19" height="19" />
                    <span>Search</span>
                </button>
                <a href="${pageContext.request.contextPath}/patients" class="btn secondary">Clear</a>
            </form>
            <c:choose>
                <c:when test="${not empty patients}">
                    <div class="data-table-container">
                    <table class="data-table">
                        <thead>
                            <tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${patients}">
                                <tr>
                                    <td>${p.id}</td>
                                    <td>${p.fullName}</td>
                                    <td>${p.email}</td>
                                    <td>${p.phoneNumber}</td>
                                    <td class="actions-cell">
                                        <a href="${pageContext.request.contextPath}/patient/load?id=${p.id}" class="btn small secondary">
                                            <img src="${pageContext.request.contextPath}/assets/icons/edit.svg" alt="" class="table-action-icon" width="17" height="17" />
                                            <span>Edit</span>
                                        </a>
                                        <form method="post" action="${pageContext.request.contextPath}/patient/delete" style="display:inline;" onsubmit="return confirm('Delete this patient?');">
                                            <input type="hidden" name="id" value="${p.id}" />
                                            <button type="submit" class="btn danger small">
                                                <img src="${pageContext.request.contextPath}/assets/icons/trash.svg" alt="" class="table-action-icon" width="17" height="17" />
                                                <span>Delete</span>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p>No patients found.</p>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</body>
</html>
