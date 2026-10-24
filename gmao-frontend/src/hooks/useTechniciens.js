import { useQuery } from '@tanstack/react-query'
import { fetchTechniciens } from '../api/technicienApi'

export const TECHNICIENS_KEY = ['techniciens']

export const useTechniciens = () =>
  useQuery({
    queryKey: TECHNICIENS_KEY,
    queryFn:  fetchTechniciens,
    staleTime: 1000 * 60 * 5, // 5 min
  })
