# Appverse AI - Smart App Marketplace & AI- Powered Analytics Platform

Appverse AI is a full - stack application marketplace where developers can publish apps, user can discover and review them, and an AI layer adds sentiment analysis, fake-review detection, personalized recommendations, and a conversational assistant on top of the core marketplace experience.

Built as a capstone project for the JAva Full Stack Development program, this project demonstrates production - style backend architecture  , paired with a modern React frontend.

## Tech Stack
# Backend
Java 17/ Spring Boot 3.2
Spring Security with JWT  for stateless authentication
Spring Data JPS / Hibernate for persistence
MySQL as relational database 
Lombok for boilerplate reduction (builders, getters/setters)
ModelMapper for entity - DTO mapping
Springdoc openAPI for API documentation
JavaMailSender for Gmail OTP- based email verification 
RestTemplate for outbound HTTP calls to the Gemini AI API
Junit 5 + Mockito +AssertJ for unit testing

# Frontend
React 18
React Reouter v6 for client side routing
Axios with a custom 'AuthContext' for API calls and auth state management

# AI / External Integrations
Google Gemini API  for AI chatbot
Custom rule-based NLP layer for sentiment analysis and rating prediction

# Tools
STS (Spring Tool Suite) and VS Code as primary IDEs
Git / GitHub for version control
Developed on Windows
