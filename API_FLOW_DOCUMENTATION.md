# Complete API Flow: College Registration → Payment → Dashboard
## Production-Ready Frontend Integration Guide

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Complete User Journey](#complete-user-journey)
3. [Frontend Integration Guide](#frontend-integration-guide)
4. [Payment Gateway Integration (Razorpay)](#payment-gateway-integration-razorpay)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Error Handling & Edge Cases](#error-handling--edge-cases)
7. [Subscription Management Flows](#subscription-management-flows)
8. [Email Notifications](#email-notifications)
9. [Security & Access Control](#security--access-control)

---

## Overview

This document describes the complete API flow from college registration through payment processing to accessing the college admin dashboard, with detailed frontend integration instructions and user interaction flows.

### Key Features

✅ **Automatic Subscription Activation** - Subscription activates automatically after successful payment  
✅ **Payment Gateway Integration** - Ready for Razorpay integration (currently simulated)  
✅ **Webhook Support** - Payment gateway webhooks for status updates  
✅ **Email Notifications** - Automated emails for all subscription/payment events  
✅ **Subscription Renewal** - Automated renewal reminders and invoice generation  
✅ **Grace Period** - Configurable grace period after subscription expiry  
✅ **Access Control** - Subscription-based API access filtering  

---

## Complete User Journey

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    COLLEGE REGISTRATION                          │
│  User fills registration form → POST /api/v1/auth/register-college│
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: Registration Success                                  │
│  - College entity created                                       │
│  - College Admin user created                                   │
│  - Redirect to login page                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: Login                                                 │
│  POST /api/v1/auth/login                                       │
│  - Returns JWT tokens                                          │
│  - Returns subscription status (NONE initially)                │
│  - Frontend checks: canAccessCoreApis = false                  │
│  - Redirect to subscription selection page                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: View Subscription Plans                               │
│  GET /api/v1/subscription-plans                                │
│  - Display plans with pricing                                  │
│  - User selects plan and billing cycle                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: Create Subscription                                   │
│  POST /api/v1/subscriptions                                    │
│  - Subscription created (status: PENDING)                      │
│  - Auto-generate invoice                                       │
│  - Redirect to payment page                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: Payment Processing                                    │
│  Frontend: Initialize Razorpay Checkout                        │
│  Backend: POST /api/v1/payments/initiate                       │
│  - Payment processed through gateway                           │
│  - Invoice marked as PAID                                      │
│  - Subscription AUTO-ACTIVATED (status: ACTIVE)                │
│  - Email notifications sent                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 6: Dashboard Access                                      │
│  - User redirected to dashboard                                │
│  - All core APIs now accessible                                │
│  - Subscription status: ACTIVE                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Frontend Integration Guide

### 1. Registration Flow

#### **1.1 Registration Form UI**

**Frontend Components:**
- Registration form with fields:
  - College Name
  - College Email
  - College Phone
  - College Short Code
  - Country
  - Admin Name
  - Admin Email
  - Password
  - Confirm Password

**API Call:**
```javascript
// Registration API call
const registerCollege = async (formData) => {
  const response = await fetch('/api/v1/auth/register-college', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      collegeName: formData.collegeName,
      collegeEmail: formData.collegeEmail,
      collegePhone: formData.collegePhone,
      collegeShortCode: formData.collegeShortCode,
      country: formData.country,
      adminName: formData.adminName,
      adminEmail: formData.adminEmail,
      password: formData.password
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Show success message
    // Redirect to login page
    router.push('/login');
  } else {
    // Show error message
    showError(result.message);
  }
};
```

**User Experience:**
1. User fills registration form
2. Click "Register" button
3. Show loading spinner
4. On success: Show success toast → Redirect to login page
5. On error: Show error message → Keep form visible

---

### 2. Login Flow

#### **2.1 Login Form UI**

**Frontend Components:**
- Login form with email and password
- "Forgot Password" link (if implemented)
- "Remember Me" checkbox (optional)

**API Call:**
```javascript
// Login API call
const login = async (email, password) => {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password })
  });
  
  const result = await response.json();
  
  if (result.success) {
    const { user, subscription, auth } = result.data;
    
    // Store tokens
    localStorage.setItem('accessToken', auth.accessToken);
    localStorage.setItem('refreshToken', auth.refreshToken);
    
    // Store user info
    localStorage.setItem('user', JSON.stringify(user));
    
    // Check subscription status
    if (subscription.canAccessCoreApis) {
      // Redirect to dashboard
      router.push('/dashboard');
    } else {
      // Redirect to subscription selection
      router.push('/subscription/select');
    }
  } else {
    showError(result.message);
  }
};
```

**User Experience:**
1. User enters email and password
2. Click "Login" button
3. Show loading spinner
4. On success:
   - If `canAccessCoreApis = true` → Redirect to dashboard
   - If `canAccessCoreApis = false` → Redirect to subscription page
5. On error: Show error message

---

### 3. Subscription Selection Flow

#### **3.1 Subscription Plans Page**

**Frontend Components:**
- Plan cards displaying:
  - Plan type (STARTER, STANDARD, PREMIUM)
  - Billing cycle options (MONTHLY, QUARTERLY, YEARLY)
  - Price
  - Features/limits
  - "Select Plan" button

**API Call:**
```javascript
// Fetch subscription plans
const fetchPlans = async () => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('/api/v1/subscription-plans', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Group plans by type
    const plansByType = groupPlansByType(result.data);
    return plansByType;
  }
};

// Create subscription
const createSubscription = async (planType, billingCycle) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('/api/v1/subscriptions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      planType,
      billingCycle
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    const subscription = result.data;
    
    // Auto-generate invoice
    await generateInvoice(subscription.uuid);
    
    // Redirect to payment page
    router.push(`/payment?subscription=${subscription.uuid}`);
  }
};
```

**User Experience:**
1. Display plan cards with pricing
2. User selects plan type and billing cycle
3. Click "Subscribe" button
4. Show loading spinner
5. On success: Auto-generate invoice → Redirect to payment page
6. On error: Show error message

---

### 4. Payment Flow

#### **4.1 Payment Page UI**

**Frontend Components:**
- Invoice summary card:
  - Invoice number
  - Amount
  - Due date
  - Plan details
- Payment gateway selection (Razorpay)
- "Pay Now" button

**API Call - Generate Invoice:**
```javascript
// Generate invoice for subscription
const generateInvoice = async (subscriptionUuid) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(`/api/v1/invoices/generate/${subscriptionUuid}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  });
  
  const result = await response.json();
  
  if (result.success) {
    return result.data; // Invoice details
  }
};
```

**API Call - Razorpay Integration:**
```javascript
// Initialize Razorpay payment
const initiateRazorpayPayment = async (invoice) => {
  // Step 1: Create Razorpay order
  const razorpayResponse = await fetch('https://api.razorpay.com/v1/orders', {
    method: 'POST',
    headers: {
      'Authorization': `Basic ${btoa(RAZORPAY_KEY_ID + ':' + RAZORPAY_KEY_SECRET)}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      amount: invoice.amount * 100, // Convert to paise
      currency: 'INR',
      receipt: invoice.invoiceNumber,
      payment_capture: 1
    })
  });
  
  const order = await razorpayResponse.json();
  
  // Step 2: Open Razorpay Checkout
  const options = {
    key: RAZORPAY_KEY_ID,
    amount: invoice.amount * 100,
    currency: 'INR',
    name: 'College Management System',
    description: `Subscription Payment - ${invoice.planName}`,
    order_id: order.id,
    handler: async function(response) {
      // Payment successful
      await processPaymentSuccess(response, invoice);
    },
    prefill: {
      email: user.email,
      name: user.name
    },
    theme: {
      color: '#3399cc'
    },
    modal: {
      ondismiss: function() {
        // User closed the payment modal
        showError('Payment cancelled');
      }
    }
  };
  
  const razorpay = new Razorpay(options);
  razorpay.open();
};

// Process payment success
const processPaymentSuccess = async (razorpayResponse, invoice) => {
  const token = localStorage.getItem('accessToken');
  
  // Call backend to record payment
  const response = await fetch('/api/v1/payments/initiate', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      invoiceUuid: invoice.uuid,
      gateway: 'RAZORPAY',
      transactionId: razorpayResponse.razorpay_payment_id,
      amount: invoice.amount
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    const payment = result.data;
    
    // Check payment status
    if (payment.status === 'SUCCESS') {
      // Payment successful - subscription auto-activated
      showSuccess('Payment successful! Subscription activated.');
      
      // Wait a moment, then redirect to dashboard
      setTimeout(() => {
        router.push('/dashboard');
      }, 2000);
    } else {
      showError('Payment processing failed. Please contact support.');
    }
  }
};
```

**User Experience:**
1. Display invoice summary
2. User clicks "Pay Now" button
3. Razorpay checkout modal opens
4. User completes payment in Razorpay modal
5. On success:
   - Show success message
   - Subscription automatically activated
   - Redirect to dashboard after 2 seconds
6. On failure:
   - Show error message
   - Allow retry payment

---

### 5. Dashboard Access

#### **5.1 Dashboard UI**

**Frontend Components:**
- Subscription status card:
  - Current plan
  - Expiry date
  - Days remaining
  - Status (ACTIVE/EXPIRED)
- Quick stats
- Navigation menu

**API Call - Check Subscription:**
```javascript
// Get current subscription
const getCurrentSubscription = async () => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch('/api/v1/subscriptions/current', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  });
  
  const result = await response.json();
  
  if (result.success) {
    const subscription = result.data;
    
    // Check if subscription is active
    if (subscription.isActive) {
      // Show dashboard
      return subscription;
    } else {
      // Redirect to subscription page
      router.push('/subscription/expired');
    }
  }
};
```

**User Experience:**
1. User lands on dashboard
2. Display subscription status
3. If active: Show full dashboard
4. If expired: Show warning banner → Redirect to renewal page

---

## Payment Gateway Integration (Razorpay)

### Integration Steps

#### **Step 1: Add Razorpay SDK**

```html
<!-- Add to HTML head -->
<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
```

#### **Step 2: Configure Razorpay Keys**

```javascript
// config/razorpay.js
export const RAZORPAY_CONFIG = {
  keyId: process.env.REACT_APP_RAZORPAY_KEY_ID,
  keySecret: process.env.REACT_APP_RAZORPAY_KEY_SECRET, // Only for server-side
};
```

#### **Step 3: Payment Flow**

```javascript
// Complete payment flow
const handlePayment = async (invoice) => {
  try {
    // 1. Create Razorpay order (server-side recommended)
    const orderResponse = await fetch('/api/v1/payments/create-razorpay-order', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        amount: invoice.amount,
        currency: 'INR',
        receipt: invoice.invoiceNumber
      })
    });
    
    const order = await orderResponse.json();
    
    // 2. Open Razorpay Checkout
    const options = {
      key: RAZORPAY_CONFIG.keyId,
      amount: order.amount,
      currency: order.currency,
      name: 'College Management System',
      description: `Subscription Payment - ${invoice.planName}`,
      order_id: order.id,
      handler: async function(response) {
        // Payment successful
        await verifyAndRecordPayment(response, invoice);
      },
      prefill: {
        email: user.email,
        name: user.name
      },
      theme: {
        color: '#3399cc'
      }
    };
    
    const razorpay = new Razorpay(options);
    razorpay.open();
    
  } catch (error) {
    showError('Payment initialization failed');
  }
};

// Verify and record payment
const verifyAndRecordPayment = async (razorpayResponse, invoice) => {
  try {
    // Call backend to verify and record payment
    const response = await fetch('/api/v1/payments/initiate', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        invoiceUuid: invoice.uuid,
        gateway: 'RAZORPAY',
        transactionId: razorpayResponse.razorpay_payment_id,
        amount: invoice.amount,
        razorpayOrderId: razorpayResponse.razorpay_order_id,
        razorpaySignature: razorpayResponse.razorpay_signature
      })
    });
    
    const result = await response.json();
    
    if (result.success && result.data.status === 'SUCCESS') {
      showSuccess('Payment successful! Subscription activated.');
      router.push('/dashboard');
    } else {
      showError('Payment verification failed');
    }
  } catch (error) {
    showError('Payment processing failed');
  }
};
```

### Webhook Handling

**Backend Webhook Endpoint:**
```
POST /api/v1/payments/webhooks/razorpay
```

**Frontend: No action needed** - Webhooks are handled server-side automatically.

**What happens:**
1. Razorpay sends webhook to backend
2. Backend verifies webhook signature
3. Backend updates payment status
4. Backend activates subscription if payment successful
5. Backend sends email notifications

---

## API Endpoints Reference

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/register-college` | Register new college | No |
| POST | `/api/v1/auth/login` | Login user | No |
| POST | `/api/v1/auth/refresh` | Refresh access token | No |

### Subscription Plan Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/subscription-plans` | Get all active plans | Yes |
| GET | `/api/v1/subscription-plans/{id}` | Get plan by ID | Yes |
| POST | `/api/v1/subscription-plans` | Create plan (SUPER_ADMIN) | Yes |
| PUT | `/api/v1/subscription-plans/{id}` | Update plan (SUPER_ADMIN) | Yes |

### Subscription Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/subscriptions` | Create subscription | Yes |
| GET | `/api/v1/subscriptions/current` | Get current subscription | Yes |
| GET | `/api/v1/subscriptions/{uuid}` | Get subscription by UUID | Yes |
| PUT | `/api/v1/subscriptions/{uuid}` | Update subscription | Yes |
| PUT | `/api/v1/subscriptions/{uuid}/activate` | Activate subscription | Yes |
| PUT | `/api/v1/subscriptions/{uuid}/cancel` | Cancel subscription | Yes |
| POST | `/api/v1/subscriptions/{uuid}/renew` | Renew subscription | Yes |

### Invoice Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/invoices/generate/{subscriptionUuid}` | Generate invoice | Yes |
| GET | `/api/v1/invoices` | Get all invoices | Yes |
| GET | `/api/v1/invoices/{uuid}` | Get invoice by UUID | Yes |
| GET | `/api/v1/invoices/unpaid` | Get unpaid invoices | Yes |
| GET | `/api/v1/invoices/overdue` | Get overdue invoices | Yes |
| GET | `/api/v1/invoices/subscription/{subscriptionUuid}` | Get invoices by subscription | Yes |

### Payment Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/payments/initiate` | Initiate and process payment | Yes |
| POST | `/api/v1/payments` | Create payment record | Yes |
| GET | `/api/v1/payments` | Get all payments | Yes |
| GET | `/api/v1/payments/{uuid}` | Get payment by UUID | Yes |
| GET | `/api/v1/payments/invoice/{invoiceUuid}` | Get payments by invoice | Yes |
| GET | `/api/v1/payments/summary` | Get payment summary | Yes |

### Webhook Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/payments/webhooks/razorpay` | Razorpay webhook | No |
| POST | `/api/v1/payments/webhooks/stripe` | Stripe webhook | No |

---

## Error Handling & Edge Cases

### Common Error Scenarios

#### **1. Registration Errors**

```javascript
// Handle registration errors
try {
  await registerCollege(formData);
} catch (error) {
  if (error.status === 409) {
    showError('College or email already exists');
  } else if (error.status === 400) {
    showError('Invalid form data. Please check all fields.');
  } else {
    showError('Registration failed. Please try again.');
  }
}
```

#### **2. Payment Errors**

```javascript
// Handle payment errors
const handlePaymentError = (error) => {
  if (error.code === 'PAYMENT_CANCELLED') {
    showWarning('Payment was cancelled. You can try again.');
  } else if (error.code === 'PAYMENT_FAILED') {
    showError('Payment failed. Please check your payment method.');
    // Allow retry
  } else if (error.code === 'NETWORK_ERROR') {
    showError('Network error. Please check your connection and try again.');
  } else {
    showError('Payment processing error. Please contact support.');
  }
};
```

#### **3. Subscription Expired**

```javascript
// Handle expired subscription
const checkSubscriptionStatus = async () => {
  const subscription = await getCurrentSubscription();
  
  if (subscription.isExpired) {
    if (subscription.isInGracePeriod) {
      showWarning(`Subscription expired. Grace period ends in ${daysRemaining} days.`);
      // Show renewal prompt
    } else {
      showError('Subscription expired. Please renew to continue.');
      router.push('/subscription/renew');
    }
  }
};
```

### Error Response Format

```json
{
  "success": false,
  "status": 400,
  "message": "Error message",
  "errors": [
    {
      "field": "email",
      "code": "INVALID_EMAIL",
      "message": "Invalid email format"
    }
  ]
}
```

---

## Subscription Management Flows

### 1. Subscription Renewal

**User Journey:**
1. User receives renewal reminder email (7 days before expiry)
2. User clicks "Renew Now" in email or dashboard
3. Frontend shows renewal page with current plan details
4. User confirms renewal
5. New invoice generated automatically
6. User completes payment
7. Subscription renewed and extended

**API Flow:**
```javascript
// Renew subscription
const renewSubscription = async (subscriptionUuid, planType, billingCycle) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(`/api/v1/subscriptions/${subscriptionUuid}/renew`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      planType,
      billingCycle
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Auto-generate invoice
    await generateInvoice(result.data.uuid);
    // Redirect to payment
    router.push(`/payment?subscription=${result.data.uuid}`);
  }
};
```

### 2. Subscription Upgrade/Downgrade

**User Journey:**
1. User navigates to subscription settings
2. User selects new plan
3. Frontend calculates prorated amount
4. User confirms upgrade/downgrade
5. New invoice generated
6. User completes payment
7. Subscription updated

**API Flow:**
```javascript
// Update subscription plan
const updateSubscription = async (subscriptionUuid, newPlanType, newBillingCycle) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(`/api/v1/subscriptions/${subscriptionUuid}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      planType: newPlanType,
      billingCycle: newBillingCycle
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Generate invoice for difference
    await generateInvoice(result.data.uuid);
    // Redirect to payment
    router.push(`/payment?subscription=${result.data.uuid}`);
  }
};
```

### 3. Subscription Cancellation

**User Journey:**
1. User navigates to subscription settings
2. User clicks "Cancel Subscription"
3. Frontend shows confirmation dialog
4. User confirms cancellation
5. Subscription status changed to CANCELLED
6. Access revoked at end of billing period

**API Flow:**
```javascript
// Cancel subscription
const cancelSubscription = async (subscriptionUuid) => {
  const confirmed = await showConfirmDialog(
    'Are you sure you want to cancel your subscription?'
  );
  
  if (!confirmed) return;
  
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(`/api/v1/subscriptions/${subscriptionUuid}/cancel`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    }
  });
  
  const result = await response.json();
  
  if (result.success) {
    showSuccess('Subscription cancelled. Access will continue until end of billing period.');
  }
};
```

---

## Email Notifications

### Email Events

The system automatically sends emails for the following events:

1. **Invoice Generated**
   - Sent when invoice is created
   - Contains: Invoice number, amount, due date
   - Action: "Pay Now" button

2. **Payment Success**
   - Sent when payment is successful
   - Contains: Transaction ID, amount, invoice number
   - Action: "View Invoice" button

3. **Payment Failure**
   - Sent when payment fails
   - Contains: Failure reason, invoice number
   - Action: "Retry Payment" button

4. **Subscription Activated**
   - Sent when subscription is activated
   - Contains: Plan type, expiry date
   - Action: "Go to Dashboard" button

5. **Subscription Expiring Soon**
   - Sent 7 days before expiry
   - Contains: Days remaining, expiry date
   - Action: "Renew Now" button

6. **Subscription Expired**
   - Sent when subscription expires
   - Contains: Expired date, renewal instructions
   - Action: "Renew Subscription" button

### Email Template Structure

```html
<!-- Example: Payment Success Email -->
<div class="email-container">
  <h1>Payment Successful</h1>
  <p>Dear {{collegeName}},</p>
  <p>Your payment has been successfully processed.</p>
  <div class="payment-details">
    <p><strong>Invoice Number:</strong> {{invoiceNumber}}</p>
    <p><strong>Amount:</strong> {{amount}} {{currency}}</p>
    <p><strong>Transaction ID:</strong> {{transactionId}}</p>
  </div>
  <a href="{{dashboardUrl}}" class="button">Go to Dashboard</a>
</div>
```

---

## Security & Access Control

### Subscription-Based Access Control

**How it works:**
1. `SubscriptionAccessFilter` checks subscription status on every API request
2. If subscription is not active → Returns `403 Forbidden`
3. Frontend should handle 403 errors gracefully

**Frontend Implementation:**
```javascript
// API interceptor to handle subscription errors
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      const errorCode = error.response.data.message;
      
      if (errorCode === 'SUBSCRIPTION_EXPIRED') {
        // Redirect to subscription page
        router.push('/subscription/expired');
        showError('Your subscription has expired. Please renew to continue.');
      } else {
        showError('Access denied. Please check your subscription status.');
      }
    }
    
    return Promise.reject(error);
  }
);
```

### Token Management

```javascript
// Token refresh interceptor
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/api/v1/auth/refresh', {
          refreshToken
        });
        
        const { accessToken } = response.data.data.auth;
        localStorage.setItem('accessToken', accessToken);
        
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // Refresh failed - logout user
        localStorage.clear();
        router.push('/login');
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);
```

---

## Complete Flow Summary

| Step | Frontend Action | API Endpoint | Backend Action | User Sees |
|------|----------------|--------------|----------------|-----------|
| 1 | User fills registration form | `POST /api/v1/auth/register-college` | Create college & admin | Success message → Login page |
| 2 | User logs in | `POST /api/v1/auth/login` | Authenticate & return tokens | Dashboard or subscription page |
| 3 | User views plans | `GET /api/v1/subscription-plans` | Return active plans | Plan selection page |
| 4 | User selects plan | `POST /api/v1/subscriptions` | Create subscription (PENDING) | Payment page |
| 5 | Invoice auto-generated | `POST /api/v1/invoices/generate/{uuid}` | Generate invoice (UNPAID) | Invoice details |
| 6 | User pays via Razorpay | `POST /api/v1/payments/initiate` | Process payment → Auto-activate subscription | Success → Dashboard |
| 7 | User accesses dashboard | `GET /api/v1/subscriptions/current` | Check subscription status | Full dashboard access |

---

## Production Checklist

### Frontend Checklist

- [ ] Razorpay SDK integrated
- [ ] Payment flow tested
- [ ] Error handling implemented
- [ ] Token refresh mechanism
- [ ] Subscription status checks
- [ ] Email notification handling
- [ ] Webhook status updates (polling if needed)
- [ ] Loading states for all async operations
- [ ] Success/error toast notifications
- [ ] Responsive design for mobile

### Backend Checklist

- [ ] Razorpay integration completed
- [ ] Webhook signature verification
- [ ] Email service configured
- [ ] Scheduled jobs for renewal reminders
- [ ] Grace period logic tested
- [ ] Subscription access filter tested
- [ ] Error responses standardized
- [ ] Logging implemented
- [ ] Security headers configured

---

## Support & Troubleshooting

### Common Issues

**Issue: Payment succeeds but subscription not activated**
- **Solution:** Check webhook logs, verify webhook endpoint is accessible
- **Check:** Payment status in database, invoice status

**Issue: User can't access dashboard after payment**
- **Solution:** Check subscription status, verify `canAccessCoreApis` in login response
- **Check:** SubscriptionAccessFilter logs, subscription expiry date

**Issue: Email notifications not received**
- **Solution:** Check email service configuration, verify SMTP credentials
- **Check:** Email service logs, spam folder

---

## Future Enhancements

1. **Trial Periods** - Free trial subscriptions
2. **Prorated Billing** - Calculate prorated amounts for upgrades/downgrades
3. **Multiple Payment Methods** - Support for multiple gateways
4. **Subscription Analytics** - Usage tracking and analytics
5. **Auto-Renewal** - Automatic subscription renewal
6. **Discount Codes** - Promotional codes and discounts
7. **Invoice PDF Generation** - Downloadable invoice PDFs
8. **Payment Retry** - Automatic payment retry for failed payments

---

**Last Updated:** December 2024  
**Version:** 2.0 (Production Ready)
