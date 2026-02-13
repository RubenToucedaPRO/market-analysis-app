# Strategy Detail View - Visual Description

## Overview
The strategy detail view displays all parameters of a strategy in a read-only, professional format using Bootstrap 5 components.

## Layout Structure

### 1. Navigation Bar (Top)
- **Left side**: AlphaSeeker logo with graph icon
- **Right side**: 
  - "Back to List" button (outline-secondary)
  - "Edit Strategy" button (primary blue)

### 2. Page Header
- Large heading showing strategy name
- Blue badge next to name showing rule count (e.g., "3 rules")

### 3. Strategy Details Card
White card with light gray header:
- **Header**: "Strategy Details" with gear icon
- **Content**:
  - Strategy Name label (small, muted) → Large strategy name text
  - Description label (small, muted) → Description text (muted)

### 4. Execution Rules Card  
White card with light gray header:
- **Header**: "Execution Rules" with checklist icon
- **Content**: Each rule displayed in its own nested card:
  
  #### Rule Card Format
  - **Rule header**: "Rule X: [Rule Name]" with green checkmark icon
  - **Parameters displayed in columns**:
    - Subject Indicator (code like "SMA", "RSI")
    - Subject Parameter (numeric value if exists)
    - Operator (>, <, >=)
    - Target Indicator (code like "CONSTANT", "SMA")
    - Target Value (numeric value if exists)
  - **Optional**: Description field (if rule has description)

### 5. Footer
Light gray footer with copyright text centered

## Color Scheme
- Primary: Bootstrap Blue (#0d6efd)
- Success: Green (for checkmark icons)
- Muted text: Gray (#6c757d)
- Background: White with light gray (#f8f9fa) for card headers

## Example Visual Flow
```
┌─────────────────────────────────────────────────────────────┐
│ 🔼 AlphaSeeker          [Back to List] [Edit Strategy]     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Golden Cross Strategy [3 rules]                           │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │ ⚙ Strategy Details                                │   │
│  ├────────────────────────────────────────────────────┤   │
│  │ Strategy Name                                      │   │
│  │ Golden Cross Strategy                              │   │
│  │                                                     │   │
│  │ Description                                         │   │
│  │ A classic momentum strategy...                     │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │ ☑ Execution Rules                                  │   │
│  ├────────────────────────────────────────────────────┤   │
│  │ ┌──────────────────────────────────────────────┐  │   │
│  │ │ ✓ Rule 1: MA50 Above MA200                  │  │   │
│  │ │                                               │  │   │
│  │ │ Subject: SMA(50)  >  Target: SMA(200)       │  │   │
│  │ └──────────────────────────────────────────────┘  │   │
│  │                                                     │   │
│  │ ┌──────────────────────────────────────────────┐  │   │
│  │ │ ✓ Rule 2: RSI Confirmation                  │  │   │
│  │ │                                               │  │   │
│  │ │ Subject: RSI(14)  >  Target: CONSTANT(50)   │  │   │
│  │ └──────────────────────────────────────────────┘  │   │
│  │                                                     │   │
│  │ ┌──────────────────────────────────────────────┐  │   │
│  │ │ ✓ Rule 3: Volume Check                      │  │   │
│  │ │                                               │  │   │
│  │ │ Subject: VOLUME  >  Target: AVG_VOLUME(20)  │  │   │
│  │ │                                               │  │   │
│  │ │ Description:                                  │  │   │
│  │ │ Ensures trading volume is above avg...      │  │   │
│  │ └──────────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│              © 2023 AlphaSeeker. All rights reserved       │
└─────────────────────────────────────────────────────────────┘
```

## User Experience
1. User clicks on strategy name in the list view
2. Page loads showing full strategy details
3. All rule parameters are clearly visible and organized
4. User can easily navigate back to list or edit the strategy
5. Clean, professional appearance consistent with the rest of the application

## Responsive Design
- Uses Bootstrap 5 grid system
- Adapts to mobile, tablet, and desktop screens
- Cards stack vertically on smaller screens
- Navigation buttons stack appropriately on mobile
