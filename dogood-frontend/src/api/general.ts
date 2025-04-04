import { createClient } from '@supabase/supabase-js'

const ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF5eWNyaWRoeG93anlnZXlpY29hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzYwMzQxODQsImV4cCI6MjA1MTYxMDE4NH0.UCDub4d-6LtNNQpQwPaeyjxnqgyhD0IDOzO21WXFFA8";
const SUPABASE_URL = 'https://qyycridhxowjygeyicoa.supabase.co'

export const host = `http://localhost`;

export const supabase = createClient(SUPABASE_URL, ANON_KEY);