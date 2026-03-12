package com.mycompany.amstest.model;

public class Assignment {
    private int assignmentId;
    private String assetTag;
    private String assetName;
    private String userName;
    private String assignedAt;
    private String returnedAt;
    private String notes;

    public Assignment() {}

    public int getAssignmentId() { 
        return assignmentId; 
    }
    
    public void setAssignmentId(int assignmentId) { 
        this.assignmentId = assignmentId; 
    }

    public String getAssetTag() { 
        return assetTag; 
    }
    
    public void setAssetTag(String assetTag) { 
        this.assetTag = assetTag; 
    }

    public String getAssetName() { 
        return assetName; 
    }
    
    public void setAssetName(String assetName) { 
        this.assetName = assetName; 
    }

    public String getUserName() { 
        return userName; 
    }
    
    public void setUserName(String userName) { 
        this.userName = userName; 
    }

    public String getAssignedAt() { 
        return assignedAt; 
    }
    
    public void setAssignedAt(String assignedAt) { 
        this.assignedAt = assignedAt; 
    }

    public String getReturnedAt() { 
        return returnedAt; 
    }
    
    public void setReturnedAt(String returnedAt) { 
        this.returnedAt = returnedAt; 
    }

    public String getNotes() { 
        return notes; 
    }
    
    public void setNotes(String notes) { 
        this.notes = notes; 
    }
}