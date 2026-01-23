package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import simulation.SimulationEngine;

/**
 * Simple HTTP web server for the OPD Queueing Model Simulation.
 * Provides a web interface to run simulations and view results.
 */
public class WebServer {
    private HttpServer server;
    // Simplified: do not rely on SimulationResult class here; run simulation and return a simple status.
    
    public WebServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Serve static files and API endpoints
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/simulate", new SimulateHandler());
        server.createContext("/api/results", new ResultsHandler());
    }
    
    public void start() {
        server.start();
        System.out.println("Web server started on http://localhost:8080");
        System.out.println("Open your browser and navigate to: http://localhost:8080");
    }
    
    public void stop() {
        server.stop(0);
    }
    
    /**
     * Handler for static files (HTML, CSS, JS)
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/") || path.equals("/index.html")) {
                serveFile(exchange, getIndexHTML(), "text/html");
            } else if (path.equals("/style.css")) {
                serveFile(exchange, getCSS(), "text/css");
            } else if (path.equals("/script.js")) {
                serveFile(exchange, getJavaScript(), "application/javascript");
            } else {
                sendResponse(exchange, 404, "Not Found", "text/plain");
            }
        }
        
        private void serveFile(HttpExchange exchange, String content, String contentType) throws IOException {
            sendResponse(exchange, 200, content, contentType);
        }
    }
    
    /**
     * Handler for simulation API endpoint
     */
    private class SimulateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            
            try {
                // Read request body
                String requestBody = readRequestBody(exchange);
                
                // Parse parameters (simple JSON-like parsing)
                double simulationTime = parseDouble(requestBody, "simulationTime", 480.0);
                double arrivalRate = parseDouble(requestBody, "arrivalRate", 0.2);
                double serviceRate = parseDouble(requestBody, "serviceRate", 0.1);
                int numberOfDoctors = parseInt(requestBody, "numberOfDoctors", 2);
                
                // Run simulation
                SimulationEngine engine = new SimulationEngine(
                    simulationTime, arrivalRate, serviceRate, numberOfDoctors
                );
                
                // Run simulation (synchronous) and return a simple completion status
                engine.run();
                sendResponse(exchange, 200, "{\"status\":\"completed\"}", "application/json");
                
            } catch (Exception e) {
                sendResponse(exchange, 500, "Error: " + e.getMessage(), "text/plain");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handler for getting last simulation results
     */
    private class ResultsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Detailed results endpoint not implemented in this build.
            sendResponse(exchange, 404, "No simulation results available", "text/plain");
        }
    }
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private double parseDouble(String body, String key, double defaultValue) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+\\.?[0-9]*)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(body);
            if (m.find()) {
                return Double.parseDouble(m.group(1));
            }
        } catch (Exception e) {
            // Ignore
        }
        return defaultValue;
    }
    
    private int parseInt(String body, String key, int defaultValue) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(body);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            // Ignore
        }
        return defaultValue;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    
    private String getIndexHTML() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>OPD Queueing Model Simulation</title>\n" +
                "    <link rel=\"stylesheet\" href=\"/style.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>🏥 OPD Queueing Model Simulation</h1>\n" +
                "        <div class=\"form-container\">\n" +
                "            <h2>Simulation Parameters</h2>\n" +
                "            <form id=\"simulationForm\">\n" +
                "                <div class=\"form-group\">\n" +
                "                    <label for=\"simulationTime\">Simulation Time (minutes):</label>\n" +
                "                    <input type=\"number\" id=\"simulationTime\" value=\"480\" min=\"1\" step=\"1\" required>\n" +
                "                </div>\n" +
                "                <div class=\"form-group\">\n" +
                "                    <label for=\"arrivalRate\">Arrival Rate (patients/minute):</label>\n" +
                "                    <input type=\"number\" id=\"arrivalRate\" value=\"0.2\" min=\"0.01\" step=\"0.01\" required>\n" +
                "                </div>\n" +
                "                <div class=\"form-group\">\n" +
                "                    <label for=\"serviceRate\">Service Rate (patients/minute per doctor):</label>\n" +
                "                    <input type=\"number\" id=\"serviceRate\" value=\"0.1\" min=\"0.01\" step=\"0.01\" required>\n" +
                "                </div>\n" +
                "                <div class=\"form-group\">\n" +
                "                    <label for=\"numberOfDoctors\">Number of Doctors:</label>\n" +
                "                    <input type=\"number\" id=\"numberOfDoctors\" value=\"2\" min=\"1\" step=\"1\" required>\n" +
                "                </div>\n" +
                "                <button type=\"submit\" id=\"runButton\">Run Simulation</button>\n" +
                "            </form>\n" +
                "        </div>\n" +
                "        <div id=\"loading\" class=\"loading\" style=\"display: none;\">\n" +
                "            <p>Running simulation... Please wait.</p>\n" +
                "        </div>\n" +
                "        <div id=\"results\" class=\"results\" style=\"display: none;\">\n" +
                "            <h2>Simulation Results</h2>\n" +
                "            <div id=\"resultsContent\"></div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script src=\"/script.js\"></script>\n" +
                "</body>\n" +
                "</html>";
    }
    
    private String getCSS() {
        return "* {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    box-sizing: border-box;\n" +
                "}\n" +
                "\n" +
                "body {\n" +
                "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "    min-height: 100vh;\n" +
                "    padding: 20px;\n" +
                "}\n" +
                "\n" +
                ".container {\n" +
                "    max-width: 900px;\n" +
                "    margin: 0 auto;\n" +
                "    background: white;\n" +
                "    border-radius: 15px;\n" +
                "    box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                "    padding: 30px;\n" +
                "}\n" +
                "\n" +
                "h1 {\n" +
                "    color: #333;\n" +
                "    text-align: center;\n" +
                "    margin-bottom: 30px;\n" +
                "    font-size: 2.5em;\n" +
                "}\n" +
                "\n" +
                "h2 {\n" +
                "    color: #555;\n" +
                "    margin-bottom: 20px;\n" +
                "    border-bottom: 2px solid #667eea;\n" +
                "    padding-bottom: 10px;\n" +
                "}\n" +
                "\n" +
                ".form-container {\n" +
                "    margin-bottom: 30px;\n" +
                "}\n" +
                "\n" +
                ".form-group {\n" +
                "    margin-bottom: 20px;\n" +
                "}\n" +
                "\n" +
                "label {\n" +
                "    display: block;\n" +
                "    margin-bottom: 8px;\n" +
                "    color: #555;\n" +
                "    font-weight: 600;\n" +
                "}\n" +
                "\n" +
                "input[type=\"number\"] {\n" +
                "    width: 100%;\n" +
                "    padding: 12px;\n" +
                "    border: 2px solid #ddd;\n" +
                "    border-radius: 8px;\n" +
                "    font-size: 16px;\n" +
                "    transition: border-color 0.3s;\n" +
                "}\n" +
                "\n" +
                "input[type=\"number\"]:focus {\n" +
                "    outline: none;\n" +
                "    border-color: #667eea;\n" +
                "}\n" +
                "\n" +
                "button {\n" +
                "    width: 100%;\n" +
                "    padding: 15px;\n" +
                "    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "    color: white;\n" +
                "    border: none;\n" +
                "    border-radius: 8px;\n" +
                "    font-size: 18px;\n" +
                "    font-weight: 600;\n" +
                "    cursor: pointer;\n" +
                "    transition: transform 0.2s, box-shadow 0.2s;\n" +
                "}\n" +
                "\n" +
                "button:hover {\n" +
                "    transform: translateY(-2px);\n" +
                "    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);\n" +
                "}\n" +
                "\n" +
                "button:active {\n" +
                "    transform: translateY(0);\n" +
                "}\n" +
                "\n" +
                ".loading {\n" +
                "    text-align: center;\n" +
                "    padding: 20px;\n" +
                "    background: #f0f0f0;\n" +
                "    border-radius: 8px;\n" +
                "    margin: 20px 0;\n" +
                "}\n" +
                "\n" +
                ".results {\n" +
                "    margin-top: 30px;\n" +
                "    padding: 20px;\n" +
                "    background: #f9f9f9;\n" +
                "    border-radius: 8px;\n" +
                "}\n" +
                "\n" +
                ".metric-card {\n" +
                "    background: white;\n" +
                "    padding: 15px;\n" +
                "    margin: 10px 0;\n" +
                "    border-radius: 8px;\n" +
                "    border-left: 4px solid #667eea;\n" +
                "    box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" +
                "}\n" +
                "\n" +
                ".metric-label {\n" +
                "    font-weight: 600;\n" +
                "    color: #555;\n" +
                "    margin-bottom: 5px;\n" +
                "}\n" +
                "\n" +
                ".metric-value {\n" +
                "    font-size: 1.5em;\n" +
                "    color: #667eea;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "\n" +
                ".doctor-stats {\n" +
                "    margin-top: 15px;\n" +
                "    padding-top: 15px;\n" +
                "    border-top: 1px solid #ddd;\n" +
                "}\n" +
                "\n" +
                ".doctor-stat {\n" +
                "    display: inline-block;\n" +
                "    margin: 5px 10px;\n" +
                "    padding: 8px 15px;\n" +
                "    background: #e8e8ff;\n" +
                "    border-radius: 5px;\n" +
                "}";
    }
    
    private String getJavaScript() {
        return "document.getElementById('simulationForm').addEventListener('submit', async function(e) {\n" +
                "    e.preventDefault();\n" +
                "    \n" +
                "    const loading = document.getElementById('loading');\n" +
                "    const results = document.getElementById('results');\n" +
                "    const runButton = document.getElementById('runButton');\n" +
                "    \n" +
                "    // Show loading, hide results\n" +
                "    loading.style.display = 'block';\n" +
                "    results.style.display = 'none';\n" +
                "    runButton.disabled = true;\n" +
                "    runButton.textContent = 'Running...';\n" +
                "    \n" +
                "    // Get form values\n" +
                "    const simulationTime = parseFloat(document.getElementById('simulationTime').value);\n" +
                "    const arrivalRate = parseFloat(document.getElementById('arrivalRate').value);\n" +
                "    const serviceRate = parseFloat(document.getElementById('serviceRate').value);\n" +
                "    const numberOfDoctors = parseInt(document.getElementById('numberOfDoctors').value);\n" +
                "    \n" +
                "    // Prepare request\n" +
                "    const requestBody = JSON.stringify({\n" +
                "        simulationTime: simulationTime,\n" +
                "        arrivalRate: arrivalRate,\n" +
                "        serviceRate: serviceRate,\n" +
                "        numberOfDoctors: numberOfDoctors\n" +
                "    });\n" +
                "    \n" +
                "    try {\n" +
                "        const response = await fetch('/api/simulate', {\n" +
                "            method: 'POST',\n" +
                "            headers: {\n" +
                "                'Content-Type': 'application/json'\n" +
                "            },\n" +
                "            body: requestBody\n" +
                "        });\n" +
                "        \n" +
                "        if (!response.ok) {\n" +
                "            throw new Error('Simulation failed: ' + response.statusText);\n" +
                "        }\n" +
                "        \n" +
                "        const data = await response.json();\n" +
                "        displayResults(data);\n" +
                "        \n" +
                "    } catch (error) {\n" +
                "        alert('Error running simulation: ' + error.message);\n" +
                "        console.error(error);\n" +
                "    } finally {\n" +
                "        loading.style.display = 'none';\n" +
                "        runButton.disabled = false;\n" +
                "        runButton.textContent = 'Run Simulation';\n" +
                "    }\n" +
                "});\n" +
                "\n" +
                "function displayResults(data) {\n" +
                "    const resultsDiv = document.getElementById('resultsContent');\n" +
                "    \n" +
                "    let html = '<div class=\"metric-card\">' +\n" +
                "        '<div class=\"metric-label\">Total Patients Arrived</div>' +\n" +
                "        '<div class=\"metric-value\">' + data.totalPatientsArrived + '</div>' +\n" +
                "        '</div>';\n" +
                "    \n" +
                "    html += '<div class=\"metric-card\">' +\n" +
                "        '<div class=\"metric-label\">Total Patients Served</div>' +\n" +
                "        '<div class=\"metric-value\">' + data.totalPatientsServed + '</div>' +\n" +
                "        '</div>';\n" +
                "    \n" +
                "    html += '<div class=\"metric-card\">' +\n" +
                "        '<div class=\"metric-label\">Average Waiting Time</div>' +\n" +
                "        '<div class=\"metric-value\">' + data.averageWaitingTime.toFixed(2) + ' minutes</div>' +\n" +
                "        '</div>';\n" +
                "    \n" +
                "    html += '<div class=\"metric-card\">' +\n" +
                "        '<div class=\"metric-label\">Average Queue Length</div>' +\n" +
                "        '<div class=\"metric-value\">' + data.averageQueueLength.toFixed(2) + ' patients</div>' +\n" +
                "        '</div>';\n" +
                "    \n" +
                "    html += '<div class=\"metric-card\">' +\n" +
                "        '<div class=\"metric-label\">Maximum Queue Length</div>' +\n" +
                "        '<div class=\"metric-value\">' + data.maxQueueLength + ' patients</div>' +\n" +
                "        '</div>';\n" +
                "    \n" +
                "    html += '<div class=\"metric-card\">' +\n" +
                "        '<div class=\"metric-label\">Service Utilization</div>' +\n" +
                "        '<div class=\"metric-value\">' + data.serviceUtilization.toFixed(2) + '%</div>' +\n" +
                "        '</div>';\n" +
                "    \n" +
                "    if (data.doctorUtilizations && data.doctorUtilizations.length > 0) {\n" +
                "        html += '<div class=\"metric-card doctor-stats\">' +\n" +
                "            '<div class=\"metric-label\">Per-Doctor Utilization:</div>';\n" +
                "        data.doctorUtilizations.forEach((util, index) => {\n" +
                "            html += '<span class=\"doctor-stat\">Doctor ' + (index + 1) + ': ' + util.toFixed(2) + '%</span>';\n" +
                "        });\n" +
                "        html += '</div>';\n" +
                "    }\n" +
                "    \n" +
                "    resultsDiv.innerHTML = html;\n" +
                "    document.getElementById('results').style.display = 'block';\n" +
                "}";
    }
}
