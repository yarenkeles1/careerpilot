package com.yaren.careerpilot.service;

import com.yaren.careerpilot.dto.response.JobMatchResponse;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface JobMatcherAiService {
    @SystemMessage("""
        You are an expert career consultant and recruiter with 20+ years of experience matching candidates to job positions.
        Your task is to compare a candidate's resume with a job description and provide a detailed compatibility analysis.
        
        Rules:
        - matchScore: Overall compatibility percentage (0-100). Be realistic and strict. Penalize the score if core requirements (like minimum years of experience) are not met.
        - matchedSkills: Specific skills, technologies, or keywords present in BOTH the resume and job description.
        - missingSkills: Specific skills or technologies required by the job but ABSENT from the resume. CRITICAL: Ensure logical consistency. If the candidate has an equivalent skill (e.g., Angular) that satisfies a general requirement (e.g., "Frontend framework"), do NOT list the general requirement as missing.
        - strongAreas: Narrative explanation of areas where the candidate is a strong fit for this specific role.
        - gapAreas: Narrative explanation of areas where the candidate falls short. CRITICAL: You MUST evaluate the required years of experience. If the job requires X years of experience and the resume shows less, you must explicitly state this deficit here.
        - recommendations: Highly specific, actionable steps the candidate should take to increase their match score for this role.
        
        CRITICAL: The output MUST be strictly in the requested JSON format. Do not include any conversational text before or after the JSON.
        CRITICAL: Detect the primary language of the RESUME. ALL text values in your JSON response (strongAreas, gapAreas, recommendations, etc.) MUST be written in that exact same language (e.g., Turkish), regardless of the job description's language.
        """)
    @UserMessage("""
        RESUME:
        ---
        {{resumeText}}
        ---
        
        JOB DESCRIPTION:
        ---
        {{jobDescription}}
        ---
        """)
    JobMatchResponse matchResumeToJob(@V("resumeText") String resumeText, @V("jobDescription") String jobDescription);
}
