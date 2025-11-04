
    private String userName;
    private String password;
    private String name;
    private String email;
    private ArrayList<Role> role;


    // Constructor to initialize a new User object with userName, password, role, name, and email.
    public User(String userName, String password, Role role, String name, String email) {
        this.userName = userName;
        this.password = password;
        this.role = new ArrayList<Role>();
        this.role.add(role);
        this.name = name;
        this.email = email;
    }
    
    // Populates user role list.
    public void setRoles(String role) {
    	if (role.contains("student")) {
    		this.role.add(Role.student);
    	}
    	if (role.contains("admin")) {
    		this.role.add(Role.admin);
    	}
    	if (role.contains("instructor")) {
    		this.role.add(Role.instructor);
    	}
    	if (role.contains("staff")) {
    		this.role.add(Role.staff);
    	}
    	if (role.contains("reviewer")) {
    		this.role.add(Role.reviewer);
    	}
    }
    
    //Removes any null entries in user's role list
    public void cleanRoleList() {
    	while (this.role.remove(null));
    }
    
    //Returns number of roles a user has
    public int numberOfRoles() {
    	this.cleanRoleList();
    	return this.role.size();
    }
    
    //Removes a user role
    public void removeRole(Role role) {
    	this.role.remove(role);
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    //Returns roles as a string for database storage
    public String getRole() {
    	String roleList = "";
    	
    	for (int i = 0; i < role.size(); i++) {
    		if (role.get(i) != null) {
    			roleList += role.get(i).name();
    		}
    	}
    	
    	return roleList;
    } 
}
