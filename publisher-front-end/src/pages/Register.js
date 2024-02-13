import React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Alert, Row, Col, Card } from 'react-bootstrap';
import axios from 'axios';

const Register = () => {
    //const awsURL = "ec2-54-164-130-60.compute-1.amazonaws.com";
    //const publisherURL = "http://" + awsURL + ":8080";
    const awsURL = "localhost";
    const publisherURL = "http://localhost:8080";
    const [formData, setFormData] = useState({
        name: '',
        email: ''
    });

    const [show, setShow] = useState(false);
    
    const history = useNavigate();    
    
    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };
    
    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(formData);
        
        const url = publisherURL + '/add/publisher';
        const companyDetails = { companyName: formData.name }; 

        axios.post(url, companyDetails).then((response) => {
            console.log(response.data);
            if (response.status === 200) {
                console.log("Company name send to backend successfully");
                const queryString = new URLSearchParams(formData).toString();
                history(`/company?${queryString}`);
            } else if (response.status === 400 || response.status === 404) {
                console.log("Publisher not found");
            } else if (response.status === 500) {
                console.log("Publisher is not reachable");
            }
          }).catch(err => {
            console.log(err);
            setShow(true);
          });
    };

    return (
        <>
            <div className="centered">
                <Card className="text-center">
                    <Card.Header>Registration</Card.Header>
                    <Card.Body>
                        <Alert show={show} variant="danger">
                            <p>Something went wrong while registeration!</p>
                            <hr />
                            <div className="d-flex justify-content-end">
                                <Button onClick={() => setShow(false)} variant="outline-danger">Close</Button>
                            </div>
                        </Alert>
                        <Form onSubmit={handleSubmit}>
                            <Form.Group as={Row} className="mb-3" controlId="formPlaintextName">
                                <Form.Label column sm="2">Name of the Company</Form.Label>
                                <Col sm="10">
                                    <Form.Control type="text" name="name" value={formData.name} onChange={handleChange} />
                                </Col>
                            </Form.Group>
                            <Form.Group as={Row} className="mb-3" controlId="formPlaintextEmail">
                                <Form.Label column sm="2">Email Address</Form.Label>
                                <Col sm="10">
                                    <Form.Control type="email" name="email" placeholder="name@example.com"  value={formData.email} onChange={handleChange} />
                                </Col>
                            </Form.Group>
                            <Button variant="success" type="submit">Register</Button>
                        </Form>
                    </Card.Body>
                    <Card.Footer className="text-muted">(c) Ruchi | Ann | Prachi</Card.Footer>
                </Card>
            </div>
        </>
    )
}

export default Register;
