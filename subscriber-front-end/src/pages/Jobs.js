import { useState, useEffect } from 'react';
import { Table, Button } from 'react-bootstrap';
import axios from 'axios';

const Jobs = () => {
    //const subURL = "http://ec2-54-82-71-245.compute-1.amazonaws.com:8082/";
    const subURL = "http://localhost:8082/";
    const [jobs, setJobs] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
          try {
            const response = await axios.get(subURL + 'jobs');
            console.log("response data");
            console.log(response.data);
            setJobs(response.data);
          } catch (error) {
            console.error(error);
          }
        };
    
        const timer = setInterval(() => {
          fetchData();
        }, 3000);
    
        fetchData();
        return () => clearInterval(timer);
      }, []);

      return (
        <div>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Company</th>
                <th>Location</th>
                <th>Description</th>
                <th>Link</th>
              </tr>
            </thead>
            <tbody>
              {jobs.map(job => (
              <tr key={job.jobId}>
                <td>{job.jobId}</td>
                <td>{job.jobTitle}</td>
                <td>{job.companyName}</td>
                <td>{job.jobLocation}</td>
                <td>{job.description}</td>
                <td><Button variant="dark">Apply</Button></td>
              </tr>
              ))}
            </tbody>
          </Table>
        </div>
      );
    };

export default Jobs;
