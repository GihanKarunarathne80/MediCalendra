<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Patient List - MediCalendra</title>
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
                <h1>Patients List</h1>
                <p style="margin:4px 0 0 0; color:#64748b; font-size:0.95rem;">Manage registered patient records and view contact information.</p>
            </div>
            <a href="${pageContext.request.contextPath}/patient/register" class="btn primary">
                <img src="${pageContext.request.contextPath}/assets/icons/user-plus.svg" alt="" class="action-icon" width="19" height="19" />
                <span>Register New Patient</span>
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
        <section class="table-section">
            <h2>Registered Patients</h2>
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
                                    <td><strong><c:out value="${p.id}"/></strong></td>
                                    <td><c:out value="${p.fullName}"/></td>
                                    <td><c:out value="${p.email}"/></td>
                                    <td><c:out value="${p.phoneNumber}"/></td>
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
                    <p style="padding:24px 0; color:#64748b; text-align:center;">No patients found.</p>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</body>
</html>

