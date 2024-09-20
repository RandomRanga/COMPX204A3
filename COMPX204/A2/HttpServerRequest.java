class HttpServerRequest
{
    private String file = null;
    private String host = null;
    private boolean done = false;
    private int line = 0;

    public boolean isDone() { return done; }
    public String getFile() { return file; }
    public String getHost() { return host; }

    public void process(String in)
    {
	
	// process the line, setting 'done' when HttpServerSession should
	// examine the contents of the request using getFile and getHost
            
        if (in == null || in.isEmpty()) {
            // End of headers, request is complete
            done = true;
            return;
        }



        // System.out.println(in);
        String parts[] = in.split(" ");
   
        String hostname = "";

        if(parts[0].compareTo("GET") == 0){
            file = parts[1].substring(1);
        }

        if(parts[0].compareTo("Host:") == 0){
            hostname = parts[1].substring(4);
        }

        if(file.endsWith("/") || file.isEmpty()){
            file += "index.html";
        }

      
        line++;
        
    }
}